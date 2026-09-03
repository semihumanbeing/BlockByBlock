package com.dahee.blockbyblock.core.utils

import com.dahee.blockbyblock.domain.model.MoldCapacityUnit
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import kotlin.math.roundToInt

object MoldCapacityFormatter {
    fun formatPreset(preset: MoldGridPreset, unit: MoldCapacityUnit, customLabel: String = "Custom"): String {
        return when (preset) {
            MoldGridPreset.ML_500 -> if (unit == MoldCapacityUnit.ML) "500ml" else "2 Cup"
            MoldGridPreset.ML_250 -> if (unit == MoldCapacityUnit.ML) "250ml" else "1 Cup"
            MoldGridPreset.ML_125 -> if (unit == MoldCapacityUnit.ML) "125ml" else "1/2 Cup"
            MoldGridPreset.ML_30 -> if (unit == MoldCapacityUnit.ML) "30ml" else "2 Tbsp"
            MoldGridPreset.CUSTOM -> customLabel
        }
    }

    fun formatCapacity(capacityMl: Int, unit: MoldCapacityUnit): String {
        if (unit == MoldCapacityUnit.ML) {
            return "${capacityMl}ml"
        }

        return when (capacityMl) {
            500 -> "2 Cup"
            250 -> "1 Cup"
            125 -> "1/2 Cup"
            60 -> "1/4 Cup"
            30 -> "2 Tbsp"
            15 -> "1 Tbsp"
            else -> {
                if (capacityMl >= 125) {
                    val cups = capacityMl / 250.0
                    val rounded = (cups * 10).roundToInt() / 10.0
                    if (rounded % 1.0 == 0.0) {
                        "${rounded.toInt()} Cup"
                    } else if (rounded == 0.5) {
                        "1/2 Cup"
                    } else if (rounded == 1.5) {
                        "1.5 Cup"
                    } else {
                        "${rounded} Cup"
                    }
                } else {
                    val tbsp = (capacityMl / 15.0).roundToInt().coerceAtLeast(1)
                    "$tbsp Tbsp"
                }
            }
        }
    }

    fun stepCapacity(currentMl: Int, delta: Int, unit: MoldCapacityUnit): Int {
        val safeCurrent = currentMl.coerceAtLeast(0)
        if (unit == MoldCapacityUnit.ML) {
            val step = 10
            return (safeCurrent + delta * step).coerceIn(10, 2000)
        } else {
            return if (delta > 0) {
                if (safeCurrent >= 125) {
                    (safeCurrent + 125).coerceAtMost(2000)
                } else if (safeCurrent <= 0) {
                    15
                } else {
                    val next = safeCurrent + 15
                    if (next > 125) 125 else next
                }
            } else {
                if (safeCurrent > 125) {
                    (safeCurrent - 125).coerceAtLeast(125)
                } else {
                    (safeCurrent - 15).coerceAtLeast(15)
                }
            }
        }
    }
}
