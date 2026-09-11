package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.core.utils.getCurrentEpochMillis
import com.dahee.blockbyblock.data.remote.dto.CreateMealPresetRequest
import com.dahee.blockbyblock.data.remote.dto.DailyMealResponse
import com.dahee.blockbyblock.data.remote.dto.MealBlockItemRequest
import com.dahee.blockbyblock.data.remote.dto.MealBlockResponse
import com.dahee.blockbyblock.data.remote.dto.MealPresetResponse
import com.dahee.blockbyblock.data.remote.dto.MealSlotResponse
import com.dahee.blockbyblock.data.remote.dto.SaveMealSlotRequest
import com.dahee.blockbyblock.data.remote.service.MealApiService
import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealBlockStatus
import com.dahee.blockbyblock.domain.model.MealPreset
import com.dahee.blockbyblock.domain.model.MealSlotRecord
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.domain.repository.MealRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetworkMealRecordRepository(
    private val apiService: MealApiService = MealApiService(),
    initialRecords: List<DayMealRecord> = emptyList(),
    initialPresets: List<MealPreset> = emptyList()
) : MealRecordRepository {

    private val _records = MutableStateFlow(initialRecords)
    private val _presets = MutableStateFlow(initialPresets)

    override fun observeMealRecords(): Flow<List<DayMealRecord>> = _records.asStateFlow()

    override suspend fun getMealRecords(): List<DayMealRecord> = _records.value

    override suspend fun getMealRecordByDate(dateString: String): DayMealRecord? {
        val existing = _records.value.find { it.dateString == dateString }
        if (existing != null) return existing
        return fetchDailyMeal(dateString).getOrNull()
    }

    override suspend fun fetchDailyMeal(dateString: String): Result<DayMealRecord?> {
        return apiService.getDailyMeal(dateString).map { response ->
            val record = mapDailyResponseToRecord(response)
            _records.update { current ->
                val filtered = current.filterNot { it.dateString == dateString }
                filtered + record
            }
            record
        }
    }

    override suspend fun fetchWeeklyMeals(startDate: String): Result<List<DayMealRecord>> {
        return apiService.getWeeklyMeals(startDate).map { response ->
            val records = response.days.map { mapDailyResponseToRecord(it) }
            _records.update { current ->
                val newDates = records.map { it.dateString }.toSet()
                val remaining = current.filterNot { it.dateString in newDates }
                remaining + records
            }
            records
        }
    }

    override suspend fun saveMealRecord(record: DayMealRecord) {
        val slots = listOf(
            record.breakfast,
            record.lunch,
            record.dinner,
            record.snack,
            record.extra
        )

        for (slot in slots) {
            val blocksReq = slot.blocks.mapIndexed { index, block ->
                MealBlockItemRequest(
                    blockId = block.blockId.toLongOrNull() ?: 0L,
                    quantity = 1,
                    sortOrder = index
                )
            }
            if (blocksReq.isNotEmpty() || slot.memo.isNotBlank() || slot.customTitle.isNotBlank()) {
                val req = SaveMealSlotRequest(
                    customTitle = slot.customTitle.ifBlank { null },
                    memo = slot.memo.ifBlank { null },
                    blocks = blocksReq
                )
                val res = apiService.saveMealSlot(
                    date = record.dateString,
                    mealType = slot.mealType.name,
                    request = req
                )
                res.getOrThrow()
            } else {
                val res = apiService.deleteMealSlot(record.dateString, slot.mealType.name)
                res.getOrThrow()
            }
        }

        _records.update { current ->
            val filtered = current.filterNot { it.dateString == record.dateString }
            filtered + record
        }
    }

    override suspend fun deleteMealRecord(id: String) {
        val target = _records.value.find { it.id == id || it.dateString == id }
        if (target != null) {
            for (type in MealType.entries) {
                apiService.deleteMealSlot(target.dateString, type.name)
            }
        }
        _records.update { it.filterNot { rec -> rec.id == id || rec.dateString == id } }
    }

    override fun observeMealPresets(): Flow<List<MealPreset>> = _presets.asStateFlow()

    override suspend fun getMealPresets(): List<MealPreset> = _presets.value

    override suspend fun fetchMealPresets(): Result<List<MealPreset>> {
        return apiService.getMealPresets().map { list ->
            val mapped = list.map { mapPresetResponseToModel(it) }
            _presets.value = mapped
            mapped
        }
    }

    override suspend fun saveMealPreset(preset: MealPreset) {
        val blocksReq = preset.blocks.mapIndexed { index, block ->
            MealBlockItemRequest(
                blockId = block.blockId.toLongOrNull() ?: 0L,
                quantity = 1,
                sortOrder = index
            )
        }
        val req = CreateMealPresetRequest(
            name = preset.name,
            memo = preset.memo.ifBlank { null },
            blocks = blocksReq
        )
        val result = apiService.createMealPreset(req)
        result.onSuccess { res ->
            val created = mapPresetResponseToModel(res)
            _presets.update { listOf(created) + it }
        }.onFailure {
            _presets.update { listOf(preset) + it }
        }
    }

    override suspend fun deleteMealPreset(id: String) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            apiService.deleteMealPreset(numericId)
        }
        _presets.update { list -> list.filterNot { it.id == id } }
    }

    private fun mapDailyResponseToRecord(response: DailyMealResponse): DayMealRecord {
        return DayMealRecord(
            id = "day_${response.dateString}",
            dateString = response.dateString,
            breakfast = mapSlotResponseToRecord(MealType.BREAKFAST, response.slots[MealType.BREAKFAST.name]),
            lunch = mapSlotResponseToRecord(MealType.LUNCH, response.slots[MealType.LUNCH.name]),
            dinner = mapSlotResponseToRecord(MealType.DINNER, response.slots[MealType.DINNER.name]),
            snack = mapSlotResponseToRecord(MealType.SNACK, response.slots[MealType.SNACK.name]),
            extra = mapSlotResponseToRecord(MealType.EXTRA, response.slots[MealType.EXTRA.name]),
            updatedAt = getCurrentEpochMillis()
        )
    }

    private fun mapSlotResponseToRecord(type: MealType, res: MealSlotResponse?): MealSlotRecord {
        if (res == null) return MealSlotRecord(type)
        val sortedBlocks = res.blocks.sortedBy { it.sortOrder }
        val blocks = sortedBlocks.flatMap { b ->
            val placementId = b.id?.toString() ?: "slot_${res.id}_block_${b.blockId}_${b.sortOrder}"
            val status = MealBlockStatus.from(b.blockStatus)
            (1..b.quantity.coerceAtLeast(1)).map { q ->
                MealBlockItem(
                    instanceId = if (q == 1) placementId else "${placementId}_$q",
                    blockId = b.blockId.toString(),
                    blockName = b.name,
                    blockColorHex = b.blockColorHex,
                    moldCapacityMl = b.moldCapacityMl,
                    moldCellCount = b.moldCellCount,
                    sortOrder = b.sortOrder,
                    currentStock = b.currentStock,
                    isDeleted = b.isDeleted,
                    blockStatus = status
                )
            }
        }
        return MealSlotRecord(
            mealType = type,
            blocks = blocks,
            memo = res.memo ?: "",
            customTitle = res.customTitle ?: ""
        )
    }

    private fun mapPresetResponseToModel(res: MealPresetResponse): MealPreset {
        val sortedBlocks = res.blocks.sortedBy { it.sortOrder }
        val blocks = sortedBlocks.flatMap { b ->
            val placementId = b.id?.toString() ?: "preset_${res.id}_block_${b.blockId}_${b.sortOrder}"
            val status = MealBlockStatus.from(b.blockStatus)
            (1..b.quantity.coerceAtLeast(1)).map { q ->
                MealBlockItem(
                    instanceId = if (q == 1) placementId else "${placementId}_$q",
                    blockId = b.blockId.toString(),
                    blockName = b.name,
                    blockColorHex = b.blockColorHex,
                    moldCapacityMl = b.moldCapacityMl,
                    moldCellCount = b.moldCellCount,
                    sortOrder = b.sortOrder,
                    currentStock = b.currentStock,
                    isDeleted = b.isDeleted,
                    blockStatus = status
                )
            }
        }
        return MealPreset(
            id = res.id.toString(),
            name = res.name,
            blocks = blocks,
            memo = res.memo ?: "",
            createdAt = res.createdAt?.let { com.dahee.blockbyblock.core.utils.parseIsoToEpochMillis(it) } ?: getCurrentEpochMillis()
        )
    }
}
