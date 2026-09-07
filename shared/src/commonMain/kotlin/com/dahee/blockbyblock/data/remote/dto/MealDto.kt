package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealBlockItemRequest(
    val blockId: Long,
    val quantity: Int = 1,
    val sortOrder: Int = 0
)

@Serializable
data class MealBlockResponse(
    val blockId: Long,
    val name: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val blockColorHex: String,
    val quantity: Int = 1,
    val sortOrder: Int = 0
)

@Serializable
data class SaveMealSlotRequest(
    val customTitle: String? = null,
    val memo: String? = null,
    val blocks: List<MealBlockItemRequest> = emptyList()
)

@Serializable
data class MealSlotResponse(
    val id: Long? = null,
    val mealType: String,
    val customTitle: String? = null,
    val memo: String? = null,
    val blocks: List<MealBlockResponse> = emptyList()
)

@Serializable
data class DailyMealResponse(
    val dateString: String,
    val slots: Map<String, MealSlotResponse> = emptyMap()
)

@Serializable
data class WeeklyMealResponse(
    val weekStartDate: String,
    val days: List<DailyMealResponse> = emptyList()
)

@Serializable
data class CreateMealPresetRequest(
    val name: String,
    val memo: String? = null,
    val blocks: List<MealBlockItemRequest> = emptyList()
)

@Serializable
data class UpdateMealPresetRequest(
    val name: String? = null,
    val memo: String? = null
)

@Serializable
data class MealPresetResponse(
    val id: Long,
    val name: String,
    val memo: String? = null,
    val blocks: List<MealBlockResponse> = emptyList(),
    val createdAt: String? = null
)
