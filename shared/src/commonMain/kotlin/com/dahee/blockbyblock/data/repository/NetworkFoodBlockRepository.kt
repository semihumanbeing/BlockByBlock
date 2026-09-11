package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.data.remote.dto.BlockResponse
import com.dahee.blockbyblock.data.remote.dto.CookingInstructionRequest
import com.dahee.blockbyblock.data.remote.dto.CookingInstructionResponse
import com.dahee.blockbyblock.data.remote.dto.CreateBlockRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateBlockQuantityRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateBlockRequest
import com.dahee.blockbyblock.data.remote.service.BlockApiService
import com.dahee.blockbyblock.domain.model.CookingInstruction
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.domain.repository.FoodBlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NetworkFoodBlockRepository(
    private val apiService: BlockApiService = BlockApiService(),
    initialBlocks: List<FoodBlock> = emptyList()
) : FoodBlockRepository {

    private val _blocksFlow = MutableStateFlow(initialBlocks)

    override fun observeFoodBlocks(): Flow<List<FoodBlock>> = _blocksFlow.asStateFlow()

    override suspend fun getFoodBlocks(): List<FoodBlock> = _blocksFlow.value

    override suspend fun fetchFoodBlocks(): Result<List<FoodBlock>> {
        return apiService.getBlocks().map { list ->
            val mapped = list.map { mapResponseToFoodBlock(it) }
            _blocksFlow.value = mapped
            mapped
        }
    }

    override suspend fun saveFoodBlock(block: FoodBlock) {
        val numericId = block.id.toLongOrNull()
        val moldNumericId = block.moldId.toLongOrNull() ?: 0L

        val instructionsReq = block.cookingInstructions.mapIndexed { index, inst ->
            CookingInstructionRequest(
                toolType = inst.toolType.name,
                temperature = inst.temperature,
                timeMinutes = inst.timeMinutes ?: 0,
                timeSeconds = inst.timeSeconds ?: 0,
                sortOrder = index
            )
        }

        if (numericId != null) {
            val req = UpdateBlockRequest(
                name = block.name,
                moldId = moldNumericId,
                moldCapacityMl = block.moldCapacityMl,
                moldCellCount = block.moldCellCount,
                blockColorHex = block.blockColorHex,
                quantity = block.quantity,
                shelfLifeDays = block.shelfLifeDays,
                memo = block.memo.ifBlank { null },
                mainIngredients = block.mainIngredients,
                subIngredients = block.subIngredients,
                cookingInstructions = instructionsReq
            )
            val result = apiService.updateBlock(numericId, req)
            result.onSuccess { res ->
                val updated = mapResponseToFoodBlock(res)
                _blocksFlow.update { list -> list.map { if (it.id == block.id) updated else it } }
            }.onFailure {
                _blocksFlow.update { list -> list.map { if (it.id == block.id) block else it } }
            }
        } else {
            val req = CreateBlockRequest(
                name = block.name,
                moldId = moldNumericId,
                moldCapacityMl = block.moldCapacityMl,
                moldCellCount = block.moldCellCount,
                blockColorHex = block.blockColorHex,
                quantity = block.quantity,
                shelfLifeDays = block.shelfLifeDays,
                memo = block.memo.ifBlank { null },
                mainIngredients = block.mainIngredients,
                subIngredients = block.subIngredients,
                cookingInstructions = instructionsReq
            )
            val result = apiService.createBlock(req)
            result.onSuccess { res ->
                val created = mapResponseToFoodBlock(res)
                _blocksFlow.update { listOf(created) + it }
            }.onFailure {
                _blocksFlow.update { listOf(block) + it }
            }
        }
    }

    override suspend fun deleteFoodBlock(id: String) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            apiService.deleteBlock(numericId)
        }
        _blocksFlow.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateQuantity(id: String, delta: Int) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            val result = apiService.updateQuantity(numericId, delta = delta)
            result.onSuccess { res ->
                _blocksFlow.update { list ->
                    list.map { if (it.id == id) it.copy(quantity = res.quantity.coerceAtLeast(0)) else it }
                }
                return
            }
        }
        _blocksFlow.update { list ->
            list.map { item ->
                if (item.id == id) {
                    val newQty = (item.quantity + delta).coerceAtLeast(0)
                    item.copy(quantity = newQty)
                } else {
                    item
                }
            }
        }
    }

    private fun mapResponseToFoodBlock(res: BlockResponse): FoodBlock {
        val preset = MoldGridPreset.fromCapacity(res.moldCapacityMl)
        val instructions = res.cookingInstructions.map { inst ->
            val tool = try { CookingToolType.valueOf(inst.toolType) } catch (_: Exception) { CookingToolType.MICROWAVE }
            CookingInstruction(
                toolType = tool,
                temperature = inst.temperature,
                timeMinutes = inst.timeMinutes,
                timeSeconds = inst.timeSeconds
            )
        }

        return FoodBlock(
            id = res.id.toString(),
            name = res.name,
            moldId = res.moldId.toString(),
            moldName = "${res.moldCapacityMl}ml",
            moldCapacityMl = res.moldCapacityMl,
            moldCellCount = res.moldCellCount,
            moldColorHex = res.blockColorHex,
            moldPreset = preset,
            blockColorHex = res.blockColorHex,
            mainIngredients = res.mainIngredients,
            subIngredients = res.subIngredients,
            quantity = res.quantity,
            shelfLifeDays = res.shelfLifeDays,
            cookingInstructions = instructions,
            memo = res.memo ?: "",
            expirationDate = res.expirationDate,
            daysRemaining = res.daysRemaining,
            isExpiringSoon = res.isExpiringSoon,
            createdAt = res.createdAt?.let { com.dahee.blockbyblock.core.utils.parseIsoToEpochMillis(it) } ?: 0L,
            createdAtIso = res.createdAt
        )
    }
}
