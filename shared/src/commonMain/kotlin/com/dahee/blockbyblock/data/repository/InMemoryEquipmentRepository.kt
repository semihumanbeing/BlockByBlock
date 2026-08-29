package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.repository.EquipmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryEquipmentRepository(
    initialItems: List<Equipment> = emptyList()
) : EquipmentRepository {

    private val _equipments = MutableStateFlow<List<Equipment>>(initialItems)

    override fun getEquipments(): Flow<List<Equipment>> = _equipments.asStateFlow()

    override suspend fun getEquipmentById(id: String): Equipment? {
        return _equipments.value.find { it.id == id }
    }

    override suspend fun addEquipment(equipment: Equipment) {
        _equipments.update { current ->
            // If already exists with same preset and color, increment quantity
            val existing = current.find {
                it.moldPreset != null && it.moldPreset == equipment.moldPreset && it.moldColorHex == equipment.moldColorHex
            }
            if (existing != null) {
                current.map {
                    if (it.id == existing.id) it.copy(quantity = it.quantity + equipment.quantity) else it
                }
            } else {
                listOf(equipment) + current
            }
        }
    }

    override suspend fun updateEquipment(equipment: Equipment) {
        _equipments.update { current ->
            current.map { if (it.id == equipment.id) equipment else it }
        }
    }

    override suspend fun deleteEquipment(id: String) {
        _equipments.update { current ->
            current.filterNot { it.id == id }
        }
    }

    override suspend fun updateQuantity(id: String, delta: Int) {
        _equipments.update { current ->
            current.mapNotNull { item ->
                if (item.id == id) {
                    val newQty = item.quantity + delta
                    if (newQty <= 0) {
                        null // Remove from list if quantity becomes 0 or less
                    } else {
                        item.copy(quantity = newQty)
                    }
                } else {
                    item
                }
            }
        }
    }

    override suspend fun setQuantity(id: String, quantity: Int) {
        _equipments.update { current ->
            current.mapNotNull { item ->
                if (item.id == id) {
                    if (quantity <= 0) {
                        null
                    } else {
                        item.copy(quantity = quantity)
                    }
                } else {
                    item
                }
            }
        }
    }
}
