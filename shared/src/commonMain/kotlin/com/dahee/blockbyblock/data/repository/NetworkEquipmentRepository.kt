package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.data.remote.dto.CreateMoldRequest
import com.dahee.blockbyblock.data.remote.dto.EquipmentsResponse
import com.dahee.blockbyblock.data.remote.dto.MoldResponse
import com.dahee.blockbyblock.data.remote.dto.SyncEquipmentsRequest
import com.dahee.blockbyblock.data.remote.dto.SyncMoldItemRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateMoldRequest
import com.dahee.blockbyblock.data.remote.service.EquipmentApiService
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetworkEquipmentRepository(
    private val apiService: EquipmentApiService = EquipmentApiService(),
    initialItems: List<Equipment> = emptyList()
) : EquipmentRepository {

    private val _equipments = MutableStateFlow(initialItems)

    override fun getEquipments(): Flow<List<Equipment>> = _equipments.asStateFlow()

    override suspend fun getEquipmentById(id: String): Equipment? {
        return _equipments.value.find { it.id == id }
    }

    override suspend fun fetchEquipments(): Result<List<Equipment>> {
        return apiService.getEquipments().map { response ->
            val mapped = mapResponseToEquipments(response)
            _equipments.value = mapped
            mapped
        }
    }

    override suspend fun syncAll(equipments: List<Equipment>): Result<List<Equipment>> {
        val moldsReq = equipments.filter { it.category == EquipmentCategory.MOLD }.map { mold ->
            SyncMoldItemRequest(
                id = mold.id.toLongOrNull(),
                name = mold.name,
                preset = mold.moldPreset?.name,
                capacityMl = mold.displayCapacity,
                cellCount = mold.cellCount,
                quantity = mold.quantity,
                colorHex = mold.moldColorHex
            )
        }

        val cookingToolsReq = equipments
            .filter { it.category == EquipmentCategory.COOKING_TOOL && it.isOwned }
            .mapNotNull { it.toolType?.name }

        val request = SyncEquipmentsRequest(
            molds = moldsReq,
            cookingTools = cookingToolsReq
        )

        return apiService.syncEquipments(request).map { response ->
            val mapped = mapResponseToEquipments(response)
            _equipments.value = mapped
            mapped
        }
    }

    override suspend fun addEquipment(equipment: Equipment) {
        if (equipment.category == EquipmentCategory.MOLD) {
            val req = CreateMoldRequest(
                name = equipment.name,
                preset = equipment.moldPreset?.name,
                capacityMl = equipment.displayCapacity,
                cellCount = equipment.cellCount,
                quantity = equipment.quantity,
                colorHex = equipment.moldColorHex
            )
            val result = apiService.createMold(req)
            result.onSuccess { moldRes ->
                val newMold = mapMoldResponseToEquipment(moldRes)
                _equipments.update { listOf(newMold) + it }
            }.onFailure {
                // Fallback to local
                _equipments.update { listOf(equipment) + it }
            }
        } else {
            _equipments.update { listOf(equipment) + it }
            syncAll(_equipments.value)
        }
    }

    override suspend fun updateEquipment(equipment: Equipment) {
        val numericId = equipment.id.toLongOrNull()
        if (numericId != null && equipment.category == EquipmentCategory.MOLD) {
            val req = UpdateMoldRequest(
                name = equipment.name,
                capacityMl = equipment.displayCapacity,
                cellCount = equipment.cellCount,
                quantity = equipment.quantity,
                colorHex = equipment.moldColorHex
            )
            val result = apiService.updateMold(numericId, req)
            result.onSuccess { moldRes ->
                val updated = mapMoldResponseToEquipment(moldRes)
                _equipments.update { list -> list.map { if (it.id == equipment.id) updated else it } }
            }.onFailure {
                _equipments.update { list -> list.map { if (it.id == equipment.id) equipment else it } }
            }
        } else {
            _equipments.update { list -> list.map { if (it.id == equipment.id) equipment else it } }
        }
    }

    override suspend fun deleteEquipment(id: String) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            apiService.deleteMold(numericId)
        }
        _equipments.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateQuantity(id: String, delta: Int) {
        val currentItem = _equipments.value.find { it.id == id } ?: return
        val newQty = currentItem.quantity + delta
        setQuantity(id, newQty)
    }

    override suspend fun setQuantity(id: String, quantity: Int) {
        val numericId = id.toLongOrNull()
        if (quantity <= 0) {
            deleteEquipment(id)
            return
        }
        if (numericId != null) {
            val result = apiService.updateMoldQuantity(numericId, quantity)
            result.onSuccess { moldRes ->
                val updated = mapMoldResponseToEquipment(moldRes)
                _equipments.update { list -> list.map { if (it.id == id) updated else it } }
                return
            }
        }
        _equipments.update { list ->
            list.map { if (it.id == id) it.copy(quantity = quantity) else it }
        }
    }

    private fun mapResponseToEquipments(response: EquipmentsResponse): List<Equipment> {
        val molds = response.molds.map { mapMoldResponseToEquipment(it) }
        val tools = response.cookingTools.mapNotNull { toolName ->
            val toolType = try { CookingToolType.valueOf(toolName) } catch (_: Exception) { null }
            toolType?.let {
                Equipment(
                    id = "tool_${it.name}",
                    name = it.displayName,
                    category = EquipmentCategory.COOKING_TOOL,
                    toolType = it,
                    isOwned = true
                )
            }
        }
        return molds + tools
    }

    private fun mapMoldResponseToEquipment(res: MoldResponse): Equipment {
        val preset = res.preset?.let { runCatching { MoldGridPreset.valueOf(it) }.getOrNull() }
            ?: MoldGridPreset.fromCapacity(res.capacityMl)

        return Equipment(
            id = res.id.toString(),
            name = res.name,
            category = EquipmentCategory.MOLD,
            moldPreset = preset,
            customCapacityMl = res.capacityMl,
            cellCount = res.cellCount,
            moldColorHex = res.colorHex ?: "#BAE6FD",
            quantity = res.quantity,
            isOwned = true
        )
    }
}
