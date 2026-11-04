package com.fleettrack.app.data.model

enum class Platform(
    val primaryPackage: String,
    val alternativePackages: List<String> = emptyList(),
    val displayName: String
) {
    UBER(
        primaryPackage = "com.ubercab.driver",
        alternativePackages = listOf("com.ubercab.driver"),
        displayName = "Uber"
    ),
    RAPIDO(
        primaryPackage = "com.rapido.rider",
        alternativePackages = listOf("com.rapido.captain", "com.rapido.passenger"),
        displayName = "Rapido"
    ),
    OLA(
        primaryPackage = "com.olacabs.oladriver",
        alternativePackages = listOf("com.olacabs.customer"),
        displayName = "Ola"
    ),
    INDRIVE(
        primaryPackage = "sinet.startup.inDriver",
        alternativePackages = listOf("com.indrive.driver"),
        displayName = "inDrive"
    ),
    NAMMA_YATRI(
        primaryPackage = "in.juspay.nammayatripartner",
        alternativePackages = listOf("in.juspay.yatridriver"),
        displayName = "Namma Yatri"
    );

    val packageName: String get() = primaryPackage

    companion object {
        fun fromPackageName(pkg: String): Platform? {
            return entries.firstOrNull {
                it.primaryPackage == pkg || it.alternativePackages.contains(pkg)
            }
        }
    }
}
