package com.fleettrack.app.data.model

data class Confidence(
    val score: Double
) {
    val level: Level
        get() = when {
            score > 0.8 -> Level.HIGH
            score >= 0.5 -> Level.MEDIUM
            else -> Level.LOW
        }

    enum class Level(val label: String) {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low")
    }

    companion object {
        fun high() = Confidence(0.95)
        fun medium() = Confidence(0.65)
        fun low() = Confidence(0.3)
    }
}
