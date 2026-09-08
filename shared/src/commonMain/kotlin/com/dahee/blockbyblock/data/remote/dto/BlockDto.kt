package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CookingInstructionRequest(
    val toolType: String,
    val temperature: Int? = null,
    val timeMinutes: Int,
    val timeSeconds: Int = 0,
    val sortOrder: Int = 0
)

@Serializable
data class CookingInstructionResponse(
    val toolType: String,
    val temperature: Int? = null,
    val timeMinutes: Int,
    val timeSeconds: Int = 0,
    val sortOrder: Int = 0
)

@Serializable
data class CreateBlockRequest(
    val name: String,
    val moldId: Long,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val blockColorHex: String,
    val quantity: Int,
    val shelfLifeDays: Int,
    val memo: String? = null,
    val mainIngredients: List<String> = emptyList(),
    val subIngredients: List<String> = emptyList(),
    val cookingInstructions: List<CookingInstructionRequest> = emptyList()
)

@Serializable
data class UpdateBlockRequest(
    val name: String,
    val moldId: Long,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val blockColorHex: String,
    val quantity: Int,
    val shelfLifeDays: Int,
    val memo: String? = null,
    val mainIngredients: List<String> = emptyList(),
    val subIngredients: List<String> = emptyList(),
    val cookingInstructions: List<CookingInstructionRequest> = emptyList()
)

@Serializable
data class BlockResponse(
    val id: Long,
    val name: String,
    val moldId: Long,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val blockColorHex: String,
    val quantity: Int,
    val shelfLifeDays: Int,
    val expirationDate: String? = null,
    val daysRemaining: Long? = null,
    val isExpiringSoon: Boolean = false,
    val memo: String? = null,
    val mainIngredients: List<String> = emptyList(),
    val subIngredients: List<String> = emptyList(),
    val cookingInstructions: List<CookingInstructionResponse> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class BlockQuantityResponse(
    val id: Long,
    val quantity: Int
)

@Serializable
data class UpdateBlockQuantityRequest(
    val quantity: Int? = null,
    val delta: Int? = null
)

@Serializable
data class BlockHistoryResponse(
    val id: Long,
    val name: String,
    val moldId: Long,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val blockColorHex: String,
    val mainIngredients: List<String> = emptyList(),
    val subIngredients: List<String> = emptyList()
)
