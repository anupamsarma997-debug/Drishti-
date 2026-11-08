package com.vidya.safetyassistant.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Local Android speech adapter. Speech recognition availability and offline language packs
 * depend on the device; TextToSpeech uses the installed engine and voice data.
 */
class VoiceAssistant(
    context: Context,
    private val onRecognizedText: (String) -> Unit,
    private val onError: (String) -> Unit
) : TextToSpeech.OnInitListener, RecognitionListener {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var recognizer: SpeechRecognizer? = null
    private var ttsReady = false
    private var languageTag = "hi-IN"
    private var systemFallbackAttempted = false
    private var usingSystemFallback = false

    fun setLanguage(tag: String) {
        languageTag = tag
        val locale = Locale.forLanguageTag(tag)
        tts?.language = locale
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
        if (ttsReady) setLanguage(languageTag)
    }

    fun speak(message: String) {
        if (message.isBlank()) return
        if (ttsReady) {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "vidya-safety-warning")
        } else {
            onError("Text-to-speech engine is not ready.")
        }
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            onError("Speech recognition is not available. Install or enable a speech service on this phone.")
            return
        }
        systemFallbackAttempted = false
        usingSystemFallback = false
        startRecognition(preferOffline = true)
    }

    /**
     * Try offline recognition first. If the selected language pack is missing or
     * unsupported, retry once through Android's regular speech service without
     * forcing offline mode. The selected language is preserved.
     */
    private fun startRecognition(preferOffline: Boolean) {
        stopListening()
        recognizer = if (preferOffline &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
        } else {
            SpeechRecognizer.createSpeechRecognizer(appContext)
        }.also { it.setRecognitionListener(this) }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, preferOffline)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    fun release() {
        stopListening()
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onError(error: Int) {
        if (!systemFallbackAttempted && !usingSystemFallback) {
            systemFallbackAttempted = true
            usingSystemFallback = true
            onError("Offline voice recognition is unavailable for this language. Trying the phone's speech service…")
            runCatching { startRecognition(preferOffline = false) }
                .onFailure {
                    onError("Could not start the phone speech service. Check its permissions and try again.")
                }
            return
        }

        val message = when (error) {
            3 -> "Microphone audio problem. Check microphone permission and try again."
            6 -> "No speech detected. Speak clearly and try again."
            7 -> "Could not understand the speech. Try again or type your question."
            9 -> "Microphone permission is missing. Allow microphone access in Android settings."
            12, 13 -> "The selected language is not available in the phone's speech service. Install its voice/language pack or choose another language."
            else -> "Voice input failed (code $error). Check internet or speech-service settings, then try again."
        }
        onError(message)
    }
    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
        if (!text.isNullOrBlank()) onRecognizedText(text)
    }
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
