package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.Equipment
import kotlinx.coroutines.flow.Flow

interface EquipmentRepository {
    fun getEquipments(): Flow<List<Equipment>>
    suspend fun getEquipmentById(id: String): Equipment?
    suspend fun addEquipment(equipment: Equipment)
    suspend fun updateEquipment(equipment: Equipment)
    suspend fun deleteEquipment(id: String)
    suspend fun updateQuantity(id: String, delta: Int)
    suspend fun setQuantity(id: String, quantity: Int)
}
