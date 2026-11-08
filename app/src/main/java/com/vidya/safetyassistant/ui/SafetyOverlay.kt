package com.vidya.safetyassistant.ui

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vidya.safetyassistant.camera.CameraDetections
import com.vidya.safetyassistant.safety.SafetyStatus

@Composable
fun SafetyOverlay(
    status: SafetyStatus,
    detections: CameraDetections? = null,
    focusHint: String? = null,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "danger-flash")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "danger-alpha"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val color = when (status) {
            SafetyStatus.SAFE -> Color(0xFF20D879)
            SafetyStatus.CAUTION -> Color(0xFFFFC247)
            SafetyStatus.STOP -> Color(0xFFFF3030)
        }
        val activeAlpha = if (status == SafetyStatus.STOP) alpha else 1f

        // Main inspection frame. SAFE means no configured issue was flagged, not guaranteed safe.
        val left = size.width * 0.12f
        val top = size.height * 0.24f
        val right = size.width * 0.88f
        val bottom = size.height * 0.70f
        drawRect(
            color = color.copy(alpha = activeAlpha),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            style = Stroke(width = 4.dp.toPx())
        )

        // ML Kit returns generic object boxes, not hazard classifications.
        detections?.let { frame ->
            val sourceW = frame.imageWidth.toFloat()
            val sourceH = frame.imageHeight.toFloat()
            if (sourceW > 0f && sourceH > 0f) {
                val scale = maxOf(size.width / sourceW, size.height / sourceH)
                val offsetX = (size.width - sourceW * scale) / 2f
                val offsetY = (size.height - sourceH * scale) / 2f
                frame.objects.forEach { item ->
                    val box = RectF(
                        offsetX + item.left * sourceW * scale,
                        offsetY + item.top * sourceH * scale,
                        offsetX + item.right * sourceW * scale,
                        offsetY + item.bottom * sourceH * scale
                    )
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        box, 8.dp.toPx(), 8.dp.toPx(),
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            this.color = android.graphics.Color.CYAN
                            style = Paint.Style.STROKE
                            strokeWidth = 2.dp.toPx()
                        }
                    )
                    val label = if (item.confidence > 0f) {
                        "${item.label} ${(item.confidence * 100).toInt()}%"
                    } else item.label
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        box.left.coerceAtLeast(4.dp.toPx()),
                        (box.top - 6.dp.toPx()).coerceAtLeast(18.dp.toPx()),
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            this.color = android.graphics.Color.WHITE
                            textSize = 12.dp.toPx()
                            style = Paint.Style.FILL
                        }
                    )
                }
            }
        }

        focusHint?.takeIf { it.isNotBlank() }?.let { partName ->
            val hint = "POSSIBLE PART: $partName · NOT VISUALLY VERIFIED"
            val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.BLACK
                textSize = 11.dp.toPx()
                style = Paint.Style.FILL
            }
            val hintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.YELLOW
                style = Paint.Style.FILL
            }
            val hintWidth = (hintPaint.measureText(hint) + 20.dp.toPx())
                .coerceAtMost(size.width - 16.dp.toPx())
            val hintTop = (size.height - 54.dp.toPx()).coerceAtLeast(8.dp.toPx())
            drawContext.canvas.nativeCanvas.drawRoundRect(
                RectF(8.dp.toPx(), hintTop, 8.dp.toPx() + hintWidth, hintTop + 34.dp.toPx()),
                7.dp.toPx(), 7.dp.toPx(), hintBg
            )
            drawContext.canvas.nativeCanvas.drawText(
                hint,
                16.dp.toPx(),
                hintTop + 21.dp.toPx(),
                hintPaint
            )
        }

        val label = when (status) {
            SafetyStatus.SAFE -> "NO FLAG: Safety not verified"
            SafetyStatus.CAUTION -> "CAUTION: Verify; safety unconfirmed"
            SafetyStatus.STOP -> "STOP: Potential hazard flagged"
        }
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = android.graphics.Color.BLACK
                textSize = 15.dp.toPx()
                style = Paint.Style.FILL
            }
            val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color.copy(alpha = activeAlpha).toArgb()
                style = Paint.Style.FILL
            }
            val labelWidth = (paint.measureText(label) + 28.dp.toPx())
                .coerceAtMost(size.width * 0.76f)
            val labelHeight = 36.dp.toPx()
            drawRoundRect(
                RectF(left, (top - labelHeight).coerceAtLeast(0f), left + labelWidth, top),
                8.dp.toPx(), 8.dp.toPx(), bg
            )
            drawText(label, left + 14.dp.toPx(), top - 12.dp.toPx(), paint)
        }
    }
}
