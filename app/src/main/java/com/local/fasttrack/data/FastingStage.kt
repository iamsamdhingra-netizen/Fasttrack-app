package com.local.fasttrack.data

/**
 * Metabolic "zones" shown while a fast is active — the same rough staging
 * used by popular fasting trackers (Fed -> Fat Burning -> Ketosis -> Autophagy).
 * These are simplified, commonly-cited thresholds for general reference only,
 * not medical guidance.
 */
enum class FastingStage(val label: String, val startHour: Int) {
    FED("Digesting", 0),
    FAT_BURNING("Fat burning", 4),
    KETOSIS("Ketosis", 16),
    AUTOPHAGY("Autophagy", 24);

    companion object {
        fun forElapsedHours(hours: Double): FastingStage {
            return values().lastOrNull { hours >= it.startHour } ?: FED
        }

        /** Hours remaining until the *next* stage begins, or null if already in the last stage. */
        fun nextStageIn(hours: Double): Pair<FastingStage, Double>? {
            val next = values().firstOrNull { it.startHour > hours } ?: return null
            return next to (next.startHour - hours)
        }
    }
}
