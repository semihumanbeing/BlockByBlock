package com.dahee.blockbyblock.domain.model

import com.dahee.blockbyblock.core.utils.formatIsoToLocalDateString
import com.dahee.blockbyblock.core.utils.formatIsoToLocalDateTimeString
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CookingInstruction(
    val toolType: CookingToolType,
    val temperature: Int? = null,
    val timeMinutes: Int? = null,
    val timeSeconds: Int? = null
)

data class FoodBlock(
    val id: String,
    val name: String,
    val moldId: String,
    val moldName: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val moldColorHex: String,
    val moldPreset: MoldGridPreset? = null,
    val blockColorHex: String = "#FF7043", // 3D Food Block Color
    val mainIngredients: List<String>,
    val subIngredients: List<String> = emptyList(),
    val quantity: Int = 1,
    val shelfLifeDays: Int = 90,
    val cookingInstructions: List<CookingInstruction> = emptyList(),
    val createdAt: Long = 0L,
    val createdAtIso: String? = null,
    val memo: String = "",
    val expirationDate: String? = null,
    val daysRemaining: Long? = null,
    val isExpiringSoon: Boolean = false
) {
    val cookingToolType: CookingToolType?
        get() = cookingInstructions.firstOrNull()?.toolType

    val cookingTemperature: Int?
        get() = cookingInstructions.firstOrNull()?.temperature

    val cookingTimeMinutes: Int?
        get() = cookingInstructions.firstOrNull()?.timeMinutes

    val cookingTimeSeconds: Int?
        get() = cookingInstructions.firstOrNull()?.timeSeconds

    val formattedLocalCreatedAt: String
        get() = if (!createdAtIso.isNullOrBlank()) {
            formatIsoToLocalDateTimeString(createdAtIso)
        } else if (createdAt > 0L) {
            val instant = Instant.fromEpochMilliseconds(createdAt)
            val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val y = ldt.year
            @Suppress("DEPRECATION")
            val m = ldt.monthNumber.toString().padStart(2, '0')
            @Suppress("DEPRECATION")
            val d = ldt.dayOfMonth.toString().padStart(2, '0')
            val hh = ldt.hour.toString().padStart(2, '0')
            val mm = ldt.minute.toString().padStart(2, '0')
            "$y.$m.$d $hh:$mm"
        } else ""

    val formattedLocalExpirationDate: String
        get() = if (!expirationDate.isNullOrBlank()) {
            formatIsoToLocalDateString(expirationDate)
        } else ""
}
