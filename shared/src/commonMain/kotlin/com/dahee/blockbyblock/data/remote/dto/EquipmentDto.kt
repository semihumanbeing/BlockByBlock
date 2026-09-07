package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoldResponse(
    val id: Long,
    val name: String,
    val preset: String? = null,
    val capacityMl: Int,
    val cellCount: Int,
    val quantity: Int,
    val colorHex: String? = null
)

@Serializable
data class EquipmentsResponse(
    val molds: List<MoldResponse>,
    val cookingTools: List<String>
)

@Serializable
data class SyncMoldItemRequest(
    val id: Long? = null,
    val name: String,
    val preset: String? = null,
    val capacityMl: Int,
    val cellCount: Int,
    val quantity: Int,
    val colorHex: String? = null
)

@Serializable
data class SyncEquipmentsRequest(
    val molds: List<SyncMoldItemRequest>,
    val cookingTools: List<String>
)

@Serializable
data class CreateMoldRequest(
    val name: String,
    val preset: String? = null,
    val capacityMl: Int,
    val cellCount: Int,
    val quantity: Int,
    val colorHex: String? = null
)

@Serializable
data class UpdateMoldRequest(
    val name: String,
    val capacityMl: Int,
    val cellCount: Int,
    val quantity: Int,
    val colorHex: String? = null
)

@Serializable
data class UpdateMoldQuantityRequest(
    val quantity: Int
)
