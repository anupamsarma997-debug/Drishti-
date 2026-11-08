package com.vidya.safetyassistant.knowledge

/**
 * Provenance-first records for Vidya's technical knowledge base.
 *
 * These are storage/domain contracts, not a claim that the repository already
 * contains a complete global catalogue. A repair procedure is eligible for
 * display only when its applicability is exact and its evidence is verified.
 */
enum class KnowledgeVerification {
    UNREVIEWED,
    SOURCE_CHECKED,
    EXPERT_VERIFIED,
    REJECTED
}

enum class KnowledgeDomain {
    MOTORCYCLE, CAR, TRUCK, TRACTOR, FARM_MACHINE, PUMP, MOTOR,
    HOME_APPLIANCE, WORKSHOP_TOOL, INDUSTRIAL_MACHINE, GENERATOR, OTHER
}

data class MachineIdentity(
    val domain: KnowledgeDomain,
    val manufacturer: String,
    val model: String,
    val modelYear: Int?,
    val variant: String?,
    val engineOrPowertrain: String? = null,
    val market: String? = null,
    val serialRange: String? = null
) {
    val isExactForRepair: Boolean
        get() = manufacturer.isNotBlank() &&
            model.isNotBlank() &&
            modelYear != null &&
            modelYear in 1886..2100 &&
            !variant.isNullOrBlank()
}

data class KnowledgeSource(
    val sourceId: String,
    val title: String,
    val publisher: String,
    val canonicalUrl: String,
    val revisionOrVersion: String? = null,
    val publicationDate: String? = null,
    val pageOrSection: String? = null,
    val license: String,
    val commercialUseAllowed: Boolean,
    val redistributionAllowed: Boolean,
    val verification: KnowledgeVerification,
    val checksumSha256: String? = null
) {
    val hasTraceableIdentity: Boolean
        get() = sourceId.isNotBlank() && title.isNotBlank() &&
            publisher.isNotBlank() && canonicalUrl.startsWith("https://") &&
            license.isNotBlank()
}

data class MachinePartRecord(
    val partId: String,
    val domain: KnowledgeDomain,
    val partName: String,
    val alternateNames: List<String>,
    val function: String,
    val locationDescription: String,
    val failureSymptoms: List<String>,
    val possibleCauses: List<String>,
    val hazardNotes: List<String>,
    val applicableIdentity: MachineIdentity? = null,
    val sourceIds: List<String> = emptyList(),
    val verification: KnowledgeVerification = KnowledgeVerification.UNREVIEWED
)

data class DiagnosticCodeRecord(
    val code: String,
    val system: String,
    val meaning: String,
    val possibleCauses: List<String>,
    val applicability: MachineIdentity?,
    val sourceIds: List<String>,
    val verification: KnowledgeVerification
)

data class ManualRecord(
    val manualId: String,
    val title: String,
    val publisher: String,
    val canonicalUrl: String,
    val revision: String?,
    val language: String?,
    val applicableIdentity: MachineIdentity,
    val manualType: String,
    val license: String,
    val redistributionAllowed: Boolean,
    val verification: KnowledgeVerification
)

data class VisualTrainingAsset(
    val assetId: String,
    val canonicalSourceUrl: String,
    val sha256: String?,
    val domain: KnowledgeDomain,
    val machineIdentity: MachineIdentity?,
    val partLabel: String,
    val defectLabel: String?,
    val captureSessionId: String?,
    val annotationSchemaVersion: String,
    val license: String,
    val commercialTrainingAllowed: Boolean,
    val redistributionAllowed: Boolean,
    val consentRecorded: Boolean,
    val verification: KnowledgeVerification
)

data class TroubleshootingRecord(
    val recordId: String,
    val title: String,
    val applicableIdentity: MachineIdentity,
    val symptoms: List<String>,
    val observationsToCollect: List<String>,
    val possibleCauses: List<String>,
    val safeExternalChecks: List<String>,
    val repairProcedureSteps: List<String>,
    val stopConditions: List<String>,
    val technicianEscalation: String,
    val sourceIds: List<String>,
    val verification: KnowledgeVerification
) {
    /**
     * Only verified, exact-variant, source-backed procedures may be presented
     * as model-specific repair steps. Other records may support cautious triage only.
     */
    fun mayShowRepairProcedure(sources: Map<String, KnowledgeSource>): Boolean {
        if (verification != KnowledgeVerification.EXPERT_VERIFIED) return false
        if (!applicableIdentity.isExactForRepair) return false
        if (repairProcedureSteps.isEmpty() || sourceIds.isEmpty()) return false
        return sourceIds.all { id ->
            val source = sources[id]
            source != null &&
                source.hasTraceableIdentity &&
                source.verification == KnowledgeVerification.EXPERT_VERIFIED
        }
    }
}
