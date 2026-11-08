package com.vidya.safetyassistant.knowledge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TechnicalKnowledgeModelsTest {
    private val identity = MachineIdentity(
        domain = KnowledgeDomain.TRACTOR,
        manufacturer = "Example OEM",
        model = "TX-40",
        modelYear = 2024,
        variant = "4WD"
    )

    private val source = KnowledgeSource(
        sourceId = "manual-tx40",
        title = "TX-40 4WD Service Manual",
        publisher = "Example OEM",
        canonicalUrl = "https://example.com/manual.pdf",
        revisionOrVersion = "Rev 2",
        pageOrSection = "Section 4",
        license = "OEM permission required",
        commercialUseAllowed = false,
        redistributionAllowed = false,
        verification = KnowledgeVerification.EXPERT_VERIFIED
    )

    private fun record(
        machine: MachineIdentity = identity,
        status: KnowledgeVerification = KnowledgeVerification.EXPERT_VERIFIED,
        sourceIds: List<String> = listOf("manual-tx40")
    ) = TroubleshootingRecord(
        recordId = "tx40-no-start",
        title = "Engine does not start",
        applicableIdentity = machine,
        symptoms = listOf("Starter cranks but engine does not start"),
        observationsToCollect = listOf("Record warning lamps"),
        possibleCauses = listOf("Fuel supply issue"),
        safeExternalChecks = listOf("Check fuel level using the operator manual"),
        repairProcedureSteps = listOf("Use the exact service procedure"),
        stopConditions = listOf("Fuel leak or damaged wiring"),
        technicianEscalation = "Refer to qualified technician",
        sourceIds = sourceIds,
        verification = status
    )

    @Test fun verifiedExactModelAndSourceCanPassGate() {
        assertTrue(record().mayShowRepairProcedure(mapOf(source.sourceId to source)))
    }

    @Test fun genericOrMissingVariantCannotPassGate() {
        val generic = identity.copy(variant = null)
        assertFalse(record(machine = generic).mayShowRepairProcedure(mapOf(source.sourceId to source)))
    }

    @Test fun unverifiedProcedureCannotPassGate() {
        assertFalse(record(status = KnowledgeVerification.SOURCE_CHECKED)
            .mayShowRepairProcedure(mapOf(source.sourceId to source)))
    }

    @Test fun missingOrUnverifiedSourceCannotPassGate() {
        assertFalse(record(sourceIds = listOf("missing"))
            .mayShowRepairProcedure(mapOf(source.sourceId to source)))
        val unverified = source.copy(verification = KnowledgeVerification.SOURCE_CHECKED)
        assertFalse(record().mayShowRepairProcedure(mapOf(source.sourceId to unverified)))
    }

    @Test fun sourceMustBeTraceableHttps() {
        val bad = source.copy(canonicalUrl = "http://example.com/manual.pdf")
        assertFalse(record().mayShowRepairProcedure(mapOf(source.sourceId to bad)))
    }
}
