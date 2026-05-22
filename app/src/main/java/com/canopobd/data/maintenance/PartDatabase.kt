package com.canopobd.data.maintenance

import com.canopobd.data.model.MaintenanceType

object PartDatabase {

    data class PartInfo(
        val name: String,
        val oemPartNumber: String,
        val alternatives: List<AlternativePart>,
        val specifications: String,
        val priceRange: PriceRange,
        val whereToBuy: List<PurchaseSource>,
        val notes: String = "",
        val compatibilityNote: String = ""
    )

    data class AlternativePart(
        val partNumber: String,
        val brand: String,
        val priceRange: PriceRange,
        val qualityRating: QualityRating,
        val notes: String = ""
    )

    data class PriceRange(
        val minEur: Double,
        val maxEur: Double
    ) {
        val averageEur: Double get() = (minEur + maxEur) / 2
        fun format(): String = "${String.format("%.0f", minEur)}-${String.format("%.0f", maxEur)}\u20AC"
    }

    data class PurchaseSource(
        val name: String,
        val type: SourceType,
        val url: String = "",
        val notes: String = ""
    )

    enum class SourceType {
        OEM_DEALER,
        ONLINE_SHOP,
        AUTO_PARTS_RETAILER,
        WRECKING_YARD
    }

    enum class QualityRating(val label: String) {
        OEM("OEM / Originalausr\u00fcster"),
        ORIGINAL_EQUIVALENT("Originalausr\u00fcster-Qualit\u00e4t"),
        AFTERMARKET_BUDGET("Aftermarket (Budget)"),
        AFTERMARKET_PREMIUM("Aftermarket (Premium)")
    }

    val OIL_FILTER = PartInfo(
        name = "\u00d6lfilter",
        oemPartNumber = "Opel 13538630",
        alternatives = listOf(
            AlternativePart("Mann HU7019z", "Mann-Filter", PriceRange(8.0, 15.0), QualityRating.OEM),
            AlternativePart("Bosch P7024", "Bosch", PriceRange(6.0, 12.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Mahle OX353D", "Mahle", PriceRange(7.0, 14.0), QualityRating.OEM),
            AlternativePart("Blue Print ADG02116", "Blue Print", PriceRange(10.0, 18.0), QualityRating.OEM),
            AlternativePart("Filtron OP520/2", "Filtron", PriceRange(5.0, 10.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Dexos2 5W-30 approved, 4.5L inkl. Filter",
        priceRange = PriceRange(8.0, 18.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Originalteile, teuer aber sicher"),
            PurchaseSource("Amazon.de", SourceType.ONLINE_SHOP, notes = "Gute Auswahl, Prime-Lieferung"),
            PurchaseSource("kfzteile24.de", SourceType.ONLINE_SHOP, notes = "Gro\u00dfes Sortiment"),
            PurchaseSource("Auto Teile Teufel", SourceType.ONLINE_SHOP, notes = "Gute Preise")
        ),
        notes = "Immer Markenfilter verwenden! Billige No-Name-Filter k\u00f6nnen Motorsch\u00e4den verursachen."
    )

    val ENGINE_OIL = PartInfo(
        name = "Motor\u00f6l",
        oemPartNumber = "Opel 1942006 (5W-30 Dexos2)",
        alternatives = listOf(
            AlternativePart("Mobil 1 ESP 5W-30", "Mobil 1", PriceRange(25.0, 40.0), QualityRating.OEM, "Dexos2 approved"),
            AlternativePart("Castrol Edge 5W-30", "Castrol", PriceRange(22.0, 35.0), QualityRating.AFTERMARKET_PREMIUM, "Dexos2 approved"),
            AlternativePart("Shell Helix Ultra 5W-30", "Shell", PriceRange(22.0, 35.0), QualityRating.AFTERMARKET_PREMIUM, "Dexos2 approved"),
            AlternativePart("Total Quartz INEO 5W-30", "Total", PriceRange(20.0, 32.0), QualityRating.AFTERMARKET_PREMIUM, "Dexos2 approved"),
            AlternativePart("Liqui Moly Top Tech 5W-30", "Liqui Moly", PriceRange(25.0, 38.0), QualityRating.OEM, "Dexos2 approved")
        ),
        specifications = "Dexos2 5W-30, ACEA C3, API SN/SM, 4.5L",
        priceRange = PriceRange(20.0, 40.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original\u00f6l, ca. 12\u20AC/L"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Gute Auswahl, regelm\u00e4\u00dfig Angebote"),
            PurchaseSource("Amazon.de", SourceType.ONLINE_SHOP, notes = "G\u00fcnstige Gro\u00dfpackungen"),
            PurchaseSource("Oelwechsel.de", SourceType.ONLINE_SHOP, notes = "Spezialist f\u00fcr Motor\u00f6le")
        ),
        notes = "Dexos2 5W-30 ist Pflicht f\u00fcr A14NET! Andere Spezifikationen k\u00f6nnen Garantie gef\u00e4hrden."
    )

    val SPARK_PLUGS = PartInfo(
        name = "Z\u00fcndkerzen",
        oemPartNumber = "NGK LZKR6AP-11G",
        alternatives = listOf(
            AlternativePart("Bosch FR7HPP332", "Bosch", PriceRange(10.0, 18.0), QualityRating.OEM, "Direkter Ersatz, 0.7mm Gap"),
            AlternativePart("Denso SC16HL11", "Denso", PriceRange(12.0, 20.0), QualityRating.OEM, "Iridium, l\u00e4ngere Lebensdauer"),
            AlternativePart("Champion RC10PYPB4", "Champion", PriceRange(8.0, 15.0), QualityRating.AFTERMARKET_PREMIUM, "Platin, 0.7mm Gap"),
            AlternativePart("Beru ZKRT7AP-11G", "Beru", PriceRange(12.0, 22.0), QualityRating.OEM, "OEM von GM"),
            AlternativePart("NGK ILZKBR7A-11G", "NGK", PriceRange(12.0, 20.0), QualityRating.OEM, "Iridium, langlebig")
        ),
        specifications = "Iridium, Gap 0.7mm, 12V, M14x1.25, 26.5mm Gewindel\u00e4nge",
        priceRange = PriceRange(40.0, 80.0),
        whereToBuy = listOf(
            PurchaseSource("NGK/Bosch H\u00e4ndler", SourceType.OEM_DEALER, notes = "Originalersatzteile"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Schnelle Lieferung"),
            PurchaseSource("Bosch Automotive", SourceType.ONLINE_SHOP, notes = "Direkt vom Hersteller")
        ),
        notes = "Gap 0.7mm einstellen! Drehmoment 20-25Nm. Immer 4 St\u00fcck wechseln!"
    )

    val AIR_FILTER = PartInfo(
        name = "Luftfilter",
        oemPartNumber = "Opel 13536248",
        alternatives = listOf(
            AlternativePart("Mann C30132/1", "Mann-Filter", PriceRange(15.0, 25.0), QualityRating.OEM),
            AlternativePart("Bosch F026400132", "Bosch", PriceRange(12.0, 22.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Mahle LX3053", "Mahle", PriceRange(14.0, 24.0), QualityRating.OEM),
            AlternativePart("K&N 33-3003", "K&N", PriceRange(45.0, 65.0), QualityRating.AFTERMARKET_PREMIUM, "Waschbar, wiederverwendbar"),
            AlternativePart("Filtron AP172/4", "Filtron", PriceRange(10.0, 18.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Papierfilter-Element, 290x160x45mm",
        priceRange = PriceRange(12.0, 25.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Originalteil"),
            PurchaseSource("Amazon", SourceType.ONLINE_SHOP, notes = "Mann-Filter oft g\u00fcnstig"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Filtration"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Sofort verf\u00fcgbar")
        ),
        notes = "K&N waschbar: H\u00f6herer Luftdurchsatz, aber Wartung erforderlich."
    )

    val COOLANT = PartInfo(
        name = "K\u00fchlmittel",
        oemPartNumber = "GM Dex-Cool 12378464 (5L)",
        alternatives = listOf(
            AlternativePart("Opel 1940665", "Opel", PriceRange(25.0, 40.0), QualityRating.OEM, "Dex-Cool orange"),
            AlternativePart("Pentosin 11-2025-3090-104", "Pentosin", PriceRange(20.0, 35.0), QualityRating.OEM, "Dex-Cool kompatibel"),
            AlternativePart("ACDelco 10-9390", "ACDelco", PriceRange(18.0, 30.0), QualityRating.OEM, "GM Original"),
            AlternativePart("Prestone Dex-Cool", "Prestone", PriceRange(15.0, 25.0), QualityRating.AFTERMARKET_BUDGET, "Dex-Cool kompatibel")
        ),
        specifications = "Dex-Cool (orange), G12++, 5.7L System, -40\u00b0C Frostschutz",
        priceRange = PriceRange(15.0, 40.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original Dex-Cool"),
            PurchaseSource("Amazon", SourceType.ONLINE_SHOP, notes = "Pentosin oft g\u00fcnstig"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Praktisch sofort verf\u00fcgbar")
        ),
        notes = "Dex-Cool (orange) ist Pflicht! Niemals gr\u00fcnes Glysantin mischen! System: 5.7L"
    )

    val TRANSMISSION_FLUID = PartInfo(
        name = "Getriebe\u00f6l",
        oemPartNumber = "GM Fluid 1940182 (Dexron VI)",
        alternatives = listOf(
            AlternativePart("ACDelco 10-9395", "ACDelco", PriceRange(12.0, 20.0), QualityRating.OEM, "GM Original Dexron VI"),
            AlternativePart("Mobil 1 1940658", "Mobil 1", PriceRange(15.0, 25.0), QualityRating.OEM, "Dexron VI ATF"),
            AlternativePart("Castrol Transmax Dexron VI", "Castrol", PriceRange(12.0, 22.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Shell ATF VI", "Shell", PriceRange(10.0, 18.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Total Fluide AT 42", "Total", PriceRange(10.0, 18.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Dexron VI ATF, 2.7L f\u00fcr M32 Getriebe",
        priceRange = PriceRange(10.0, 25.0),
        whereToBuy = listOf(
            PurchaseSource("Opel/GM H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original GM Fluid"),
            PurchaseSource("Amazon", SourceType.ONLINE_SHOP, notes = "Gro\u00dfe Auswahl"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Kleinmengen verf\u00fcgbar")
        ),
        notes = "Dexron VI Pflicht f\u00fcr M32 Getriebe! Nur 2.7L ben\u00f6tigt."
    )

    val BRAKE_PADS_FRONT = PartInfo(
        name = "Bremsbel\u00e4ge vorne",
        oemPartNumber = "Opel 13501636",
        alternatives = listOf(
            AlternativePart("TRW D1428L", "TRW", PriceRange(35.0, 55.0), QualityRating.OEM, "Hohe Qualit\u00e4t, gutes Preis-Leistung"),
            AlternativePart("Akebono ACT1428", "Akebono", PriceRange(40.0, 65.0), QualityRating.OEM, "Original-Ausr\u00fcster"),
            AlternativePart("Bosch BC1428", "Bosch", PriceRange(30.0, 50.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Brembo P50073", "Brembo", PriceRange(45.0, 70.0), QualityRating.OEM, "Premium-Qualit\u00e4t"),
            AlternativePart("Jurid 1428.20", "Jurid", PriceRange(25.0, 45.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "286mm Scheibendurchmesser, 55mm Breite, 16.5mm St\u00e4rke",
        priceRange = PriceRange(30.0, 70.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original-Ausr\u00fcster"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "TRW oft g\u00fcnstig"),
            PurchaseSource("Brembo Shop", SourceType.ONLINE_SHOP, notes = "Direkt vom Hersteller"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Einbau m\u00f6glich")
        ),
        notes = "Nur Qualit\u00e4tsbel\u00e4ge verwenden! Billige Bel\u00e4ge k\u00f6nnen Bremsscheiben besch\u00e4digen."
    )

    val BRAKE_PADS_REAR = PartInfo(
        name = "Bremsbel\u00e4ge hinten",
        oemPartNumber = "Opel 13501637",
        alternatives = listOf(
            AlternativePart("TRW D1429L", "TRW", PriceRange(30.0, 50.0), QualityRating.OEM),
            AlternativePart("Akebono ACT1429", "Akebono", PriceRange(35.0, 55.0), QualityRating.OEM),
            AlternativePart("Bosch BC1429", "Bosch", PriceRange(28.0, 45.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Brembo P50072", "Brembo", PriceRange(40.0, 60.0), QualityRating.OEM),
            AlternativePart("Jurid 1429.20", "Jurid", PriceRange(22.0, 40.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "258mm Scheibendurchmesser, 45mm Breite, 15mm St\u00e4rke",
        priceRange = PriceRange(25.0, 60.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original-Ausr\u00fcster"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "TRW oft g\u00fcnstig"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Einbau m\u00f6glich")
        ),
        notes = "Heckbremsen nutzen sich langsamer ab als vorne. Regelm\u00e4\u00dfig pr\u00fcfen!"
    )

    val BRAKE_DISCS_FRONT = PartInfo(
        name = "Bremsscheiben vorne",
        oemPartNumber = "Opel 12670568",
        alternatives = listOf(
            AlternativePart("TRW L1428", "TRW", PriceRange(40.0, 70.0), QualityRating.OEM, "Bel\u00fcftet, gelocht"),
            AlternativePart("Bosch BD5588", "Bosch", PriceRange(35.0, 60.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Brembo P50073N", "Brembo", PriceRange(55.0, 85.0), QualityRating.OEM, "Premium-Qualit\u00e4t"),
            AlternativePart("ATE 24.0128-0128.1", "ATE", PriceRange(45.0, 70.0), QualityRating.OEM),
            AlternativePart("Jurid 1428V", "Jurid", PriceRange(30.0, 50.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "286mm Durchmesser, 22.2mm St\u00e4rke (neu), Min. 20.4mm",
        priceRange = PriceRange(35.0, 85.0),
        whereToBuy = listOf(
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original"),
            PurchaseSource("Brembo Shop", SourceType.ONLINE_SHOP, notes = "Premium"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Preise"),
            PurchaseSource("ATE", SourceType.ONLINE_SHOP, notes = "OEM-Qualit\u00e4t")
        ),
        notes = "Mindestst\u00e4rke beachten! Bei Belagswechsel immer Scheiben pr\u00fcfen."
    )

    val CABIN_FILTER = PartInfo(
        name = "Innenraumfilter",
        oemPartNumber = "Opel 13536247",
        alternatives = listOf(
            AlternativePart("Mann CU31006", "Mann-Filter", PriceRange(12.0, 22.0), QualityRating.OEM),
            AlternativePart("Bosch F005CD472", "Bosch", PriceRange(10.0, 18.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Mahle LA208", "Mahle", PriceRange(11.0, 20.0), QualityRating.OEM),
            AlternativePart("K&N VF2002", "K&N", PriceRange(25.0, 40.0), QualityRating.AFTERMARKET_PREMIUM, "Aktivkohle, waschbar"),
            AlternativePart("Filtron K1102", "Filtron", PriceRange(8.0, 15.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Aktivkohle oder Standard, 215x189x30mm",
        priceRange = PriceRange(8.0, 25.0),
        whereToBuy = listOf(
            PurchaseSource("Amazon", SourceType.ONLINE_SHOP, notes = "Gro\u00dfe Auswahl"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Sofort verf\u00fcgbar"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Preise")
        ),
        notes = "Aktivkohlefilter empfohlen f\u00fcr Allergiker. Alle 1-2 Jahre wechseln."
    )

    val BATTERY = PartInfo(
        name = "Batterie",
        oemPartNumber = "Opel 1201039 (59Ah/540A)",
        alternatives = listOf(
            AlternativePart("Varta Blue D59", "Varta", PriceRange(90.0, 140.0), QualityRating.OEM, "59Ah, 540A"),
            AlternativePart("Bosch S4 059", "Bosch", PriceRange(95.0, 150.0), QualityRating.OEM, "59Ah, 540A"),
            AlternativePart("Exide EA640", "Exide", PriceRange(80.0, 130.0), QualityRating.AFTERMARKET_PREMIUM, "59Ah, 540A"),
            AlternativePart("Banner Starting Bull 59Ah", "Banner", PriceRange(95.0, 155.0), QualityRating.OEM, "60Ah, 560A"),
            AlternativePart("Numax 59Ah", "Numax", PriceRange(70.0, 110.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "59Ah, 540A (EN), 12V, B13 Terminal",
        priceRange = PriceRange(70.0, 155.0),
        whereToBuy = listOf(
            PurchaseSource("Varta Partner", SourceType.AUTO_PARTS_RETAILER, notes = "Professionelle Beratung"),
            PurchaseSource("Amazon", SourceType.ONLINE_SHOP, notes = "Gute Preise"),
            PurchaseSource("ATU", SourceType.AUTO_PARTS_RETAILER, notes = "Einbau + Altbatterie-Entsorgung")
        ),
        notes = "Start-Stop-Fahrzeuge brauchen EFB oder AGM Batterie!"
    )

    val TIMING_CHAIN_KIT = PartInfo(
        name = "Steuerkettensatz",
        oemPartNumber = "Opel 12618087 / 24420398",
        alternatives = listOf(
            AlternativePart("INA 421009710", "INA", PriceRange(250.0, 400.0), QualityRating.OEM, "Ketten+Spanner+Leitschienen"),
            AlternativePart("Sachs 186726", "Sachs", PriceRange(280.0, 450.0), QualityRating.OEM),
            AlternativePart("SLM 24420398", "SLM", PriceRange(200.0, 350.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Crown 24420398", "Crown", PriceRange(180.0, 300.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Steuerkette 2L, Kettenspanner, Leitschienen, Spannschiene",
        priceRange = PriceRange(180.0, 450.0),
        whereToBuy = listOf(
            PurchaseSource("INA Shop", SourceType.ONLINE_SHOP, notes = "Schaeffler Original"),
            PurchaseSource("Sachs Partner", SourceType.ONLINE_SHOP, notes = "ZF Premium"),
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Originalteile")
        ),
        notes = "BEKANNTES PROBLEM bei A14NET! Kettenspanner oft defekt ab 80.000km. Bei Rattern SOFORT pr\u00fcfen!",
        compatibilityNote = "Nur f\u00fcr A14NET/LUJ Engine!"
    )

    val CLUTCH_KIT = PartInfo(
        name = "Kupplungssatz",
        oemPartNumber = "Opel 3247W40",
        alternatives = listOf(
            AlternativePart("Sachs 2282125", "Sachs", PriceRange(180.0, 280.0), QualityRating.OEM, "Satz mit Druckplatte+Scheibe"),
            AlternativePart("Luk 621309609", "Luk", PriceRange(190.0, 300.0), QualityRating.OEM),
            AlternativePart("Valeo 826729", "Valeo", PriceRange(170.0, 270.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Exedy 19010", "Exedy", PriceRange(200.0, 320.0), QualityRating.OEM)
        ),
        specifications = "Mit Ausr\u00fccklager, Druckplatte, Kupplungsscheibe",
        priceRange = PriceRange(170.0, 320.0),
        whereToBuy = listOf(
            PurchaseSource("Sachs/Luk H\u00e4ndler", SourceType.ONLINE_SHOP, notes = "Original-Qualit\u00e4t"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Komplett-Sets"),
            PurchaseSource("Autoteile-Markt", SourceType.ONLINE_SHOP, notes = "Gro\u00dfe Auswahl")
        ),
        notes = "Immer kompletten Satz wechseln! Ausr\u00fccklager nicht vergessen. Werkstatt empfohlen."
    )

    val THERMOSTAT = PartInfo(
        name = "Thermostat",
        oemPartNumber = "Opel 13385127",
        alternatives = listOf(
            AlternativePart("Wahler 13385127W", "Wahler", PriceRange(25.0, 45.0), QualityRating.OEM),
            AlternativePart("Vemo 50-10625", "Vemo", PriceRange(20.0, 40.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Era 350183", "Era", PriceRange(18.0, 35.0), QualityRating.AFTERMARKET_BUDGET),
            AlternativePart("Hella 309363", "Hella", PriceRange(22.0, 40.0), QualityRating.AFTERMARKET_PREMIUM)
        ),
        specifications = "82\u00b0C \u00d6ffnungstemperatur, mit Dichtung",
        priceRange = PriceRange(18.0, 45.0),
        whereToBuy = listOf(
            PurchaseSource("Wahler", SourceType.ONLINE_SHOP, notes = "OEM-Qualit\u00e4t"),
            PurchaseSource("Hella", SourceType.ONLINE_SHOP, notes = "Mahle-Tochter"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Preise")
        ),
        notes = "Elektronisch geregelt! Einfacher Wechsel, aber K\u00fchlmittel ablassen."
    )

    val WATER_PUMP = PartInfo(
        name = "Wasserpumpe",
        oemPartNumber = "Opel 13385126",
        alternatives = listOf(
            AlternativePart("Gates WPO-1192", "Gates", PriceRange(45.0, 80.0), QualityRating.OEM),
            AlternativePart("SKF VKMA35026", "SKF", PriceRange(50.0, 90.0), QualityRating.OEM),
            AlternativePart("Luk WPO-1192", "Luk", PriceRange(48.0, 85.0), QualityRating.OEM)
        ),
        specifications = "Elektrisch gesteuert, 5.7L K\u00fchlsystem",
        priceRange = PriceRange(45.0, 90.0),
        whereToBuy = listOf(
            PurchaseSource("Gates Shop", SourceType.ONLINE_SHOP, notes = "Hohe Qualit\u00e4t"),
            PurchaseSource("SKF", SourceType.ONLINE_SHOP, notes = "Originalausr\u00fcster"),
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original")
        ),
        notes = "Elektrisch geregelt, nicht mechanisch! Mit Steuerkette immer auch Wasserpumpe pr\u00fcfen."
    )

    val VALVE_COVER_GASKET = PartInfo(
        name = "Zylinderkopfhaubendichtung",
        oemPartNumber = "Opel 12618089",
        alternatives = listOf(
            AlternativePart("Elring 24336", "Elring", PriceRange(15.0, 30.0), QualityRating.OEM),
            AlternativePart("Victor Reinz 71-33940-00", "Victor Reinz", PriceRange(12.0, 25.0), QualityRating.OEM),
            AlternativePart("Payen JC520", "Payen", PriceRange(10.0, 20.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Zylinderkopfhaube, mit Dichtmasse oder vulkanisiert",
        priceRange = PriceRange(8.0, 30.0),
        whereToBuy = listOf(
            PurchaseSource("Elring", SourceType.ONLINE_SHOP, notes = "Premium-Qualit\u00e4t"),
            PurchaseSource("Victor Reinz", SourceType.ONLINE_SHOP, notes = "Dana-Gruppe"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Preise")
        ),
        notes = "Bei \u00d6lverlust an der Kopfhaube: Sofort wechseln!"
    )

    val WHEEL_BEARING_FRONT = PartInfo(
        name = "Radlager vorne",
        oemPartNumber = "Opel 93169363",
        alternatives = listOf(
            AlternativePart("SKF VKBA3656", "SKF", PriceRange(80.0, 130.0), QualityRating.OEM),
            AlternativePart("FAG 713630190", "FAG", PriceRange(75.0, 120.0), QualityRating.OEM),
            AlternativePart("SNR R155.75", "SNR", PriceRange(70.0, 115.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("Ruville 5925", "Ruville", PriceRange(65.0, 110.0), QualityRating.AFTERMARKET_PREMIUM),
            AlternativePart("NK 4414502", "NK", PriceRange(55.0, 95.0), QualityRating.AFTERMARKET_BUDGET)
        ),
        specifications = "Mit ABS-Sensor, 72mm Au\u00dfendurchmesser",
        priceRange = PriceRange(55.0, 130.0),
        whereToBuy = listOf(
            PurchaseSource("SKF Shop", SourceType.ONLINE_SHOP, notes = "Original-Qualit\u00e4t"),
            PurchaseSource("FAG Shop", SourceType.ONLINE_SHOP, notes = "Schaeffler Gruppe"),
            PurchaseSource("kfzteile24", SourceType.ONLINE_SHOP, notes = "Gute Preise"),
            PurchaseSource("Opel H\u00e4ndler", SourceType.OEM_DEALER, notes = "Original")
        ),
        notes = "Mit ABS-Sensor! Ohne Sensor funktioniert ASR/ESP nicht richtig."
    )

    fun getPartInfo(maintenanceType: MaintenanceType): PartInfo? {
        return when (maintenanceType) {
            MaintenanceType.OIL_CHANGE -> OIL_FILTER
            MaintenanceType.SPARK_PLUGS -> SPARK_PLUGS
            MaintenanceType.AIR_FILTER -> AIR_FILTER
            MaintenanceType.COOLANT -> COOLANT
            MaintenanceType.TRANSMISSION_FLUID -> TRANSMISSION_FLUID
            MaintenanceType.BRAKE_PADS -> BRAKE_PADS_FRONT
            else -> null
        }
    }

    fun searchPart(searchTerm: String): List<PartInfo> {
        val term = searchTerm.lowercase()
        return allParts.filter { part ->
            part.name.lowercase().contains(term) ||
            part.oemPartNumber.lowercase().contains(term) ||
            part.alternatives.any {
                it.partNumber.lowercase().contains(term) ||
                it.brand.lowercase().contains(term)
            }
        }
    }

    val allParts: List<PartInfo> = listOf(
        OIL_FILTER,
        ENGINE_OIL,
        SPARK_PLUGS,
        AIR_FILTER,
        COOLANT,
        TRANSMISSION_FLUID,
        BRAKE_PADS_FRONT,
        BRAKE_PADS_REAR,
        BRAKE_DISCS_FRONT,
        CABIN_FILTER,
        BATTERY,
        TIMING_CHAIN_KIT,
        CLUTCH_KIT,
        THERMOSTAT,
        WATER_PUMP,
        VALVE_COVER_GASKET,
        WHEEL_BEARING_FRONT
    )
}
