package com.fleettrack.app.data.model

enum class Platform(
    val packageName: String,
    val displayName: String
) {
    RAPIDO(
        packageName = "com.rapido.passenger",
        displayName = "Rapido"
    ),
    UBER(
        packageName = "com.ubercab.driver",
        displayName = "Uber"
    ),
    OLA(
        packageName = "com.olacabs.customer",
        displayName = "Ola"
    );

    companion object {
        fun fromPackageName(pkg: String): Platform? {
            return entries.firstOrNull { it.packageName == pkg }
        }
    }
}
