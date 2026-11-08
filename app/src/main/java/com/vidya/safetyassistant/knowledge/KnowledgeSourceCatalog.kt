package com.vidya.safetyassistant.knowledge

/**
 * Curated discovery sources only. These entries are not imported datasets,
 * OEM manuals, or approved repair procedures.
 */
object KnowledgeSourceCatalog {
    val sources: List<KnowledgeSource> = listOf(
        KnowledgeSource(
            sourceId = "open-repair-alliance-ords-v0.3",
            title = "Open Repair Alliance Open Repair Data Standard v0.3 dataset",
            publisher = "Open Repair Alliance",
            canonicalUrl = "https://openrepair.org/open-data/downloads/",
            revisionOrVersion = "ORDS v0.3; page updated October 2026",
            license = "CC BY-SA 4.0",
            commercialUseAllowed = true,
            redistributionAllowed = true,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "obdex-generic-obd",
            title = "OBDex generic OBD-II codes and PIDs",
            publisher = "OBDex contributors",
            canonicalUrl = "https://github.com/foerbsnavi/obdex",
            license = "CC0-1.0 data; MIT tooling",
            commercialUseAllowed = true,
            redistributionAllowed = true,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "nhtsa-vpic",
            title = "Vehicle Product Information Catalog API",
            publisher = "National Highway Traffic Safety Administration",
            canonicalUrl = "https://vpic.nhtsa.dot.gov/api/",
            revisionOrVersion = "API 4.10; page reports 17 October 2026 update",
            license = "Review current API and data terms before reuse",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "mimii-dg",
            title = "MIMII DG industrial machine sound dataset",
            publisher = "Hitachi Ltd. via Zenodo",
            canonicalUrl = "https://zenodo.org/records/6529888",
            revisionOrVersion = "Version 1.0; 9 May 2022",
            license = "CC BY-NC-SA 4.0",
            commercialUseAllowed = false,
            redistributionAllowed = true,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "dgt-mechanic-agricultural-machinery",
            title = "Mechanic Agricultural Machinery CTS 2.0 curriculum",
            publisher = "Directorate General of Training, India",
            canonicalUrl = "https://www.dgt.gov.in/sites/default/files/2023-12/Mech.%20Agricultural%20Machinery_CTS2.0_NSQF-4.pdf",
            license = "Reuse terms require review; curriculum is not an OEM service manual",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "icar-agricultural-engineering-catalogue",
            title = "Agricultural Engineering machinery catalogue",
            publisher = "Indian Council of Agricultural Research",
            canonicalUrl = "https://icar.org.in/sites/default/files/inline-files/AgriculturalEngineering_1.pdf",
            license = "Reuse terms require review; catalogue is not a repair procedure",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        )

        ,
        KnowledgeSource(
            sourceId = "hero-india-owner-manuals",
            title = "Hero MotoCorp India two-wheeler owner manuals",
            publisher = "Hero MotoCorp",
            canonicalUrl = "https://www.heromotocorp.com/en-in/services/owner-manual.html",
            license = "Website download terms apply; no redistribution permission assumed",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "honda2wheelers-india-owner-manuals",
            title = "Honda Motorcycle and Scooter India owner manuals",
            publisher = "Honda Motorcycle and Scooter India",
            canonicalUrl = "https://www.honda2wheelersindia.com/resources/owners-manual",
            license = "Website download terms apply; no redistribution permission assumed",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "honda-motopub-india-service-publications",
            title = "Honda Motorcycle and Scooter India service publications",
            publisher = "Honda Motorcycle and Scooter India via MotoPub",
            canonicalUrl = "https://www.hondamotopub.com/HMSI",
            license = "Licence agreement required before download; no redistribution permission assumed",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "tvs-india-user-manuals",
            title = "TVS Motor India vehicle user manuals",
            publisher = "TVS Motor Company",
            canonicalUrl = "https://www.tvsmotor.com/our-service/user-manual",
            license = "Website download terms apply; no redistribution permission assumed",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        ),
        KnowledgeSource(
            sourceId = "bajaj-india-owner-manuals",
            title = "Bajaj Auto India owner manuals",
            publisher = "Bajaj Auto",
            canonicalUrl = "https://www.bajajauto.com/customer-service/owners-manual",
            license = "Website download terms apply; no redistribution permission assumed",
            commercialUseAllowed = false,
            redistributionAllowed = false,
            verification = KnowledgeVerification.SOURCE_CHECKED
        )
    )

    fun find(sourceId: String): KnowledgeSource? =
        sources.firstOrNull { it.sourceId == sourceId }
}
