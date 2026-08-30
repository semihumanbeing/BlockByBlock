package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.repository.FoodBlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryFoodBlockRepository : FoodBlockRepository {
    private val _blocksFlow = MutableStateFlow<List<FoodBlock>>(emptyList())

    override fun observeFoodBlocks(): Flow<List<FoodBlock>> = _blocksFlow.asStateFlow()

    override suspend fun getFoodBlocks(): List<FoodBlock> = _blocksFlow.value

    override suspend fun saveFoodBlock(block: FoodBlock) {
        val current = _blocksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == block.id }
        if (index >= 0) {
            current[index] = block
        } else {
            current.add(0, block)
        }
        _blocksFlow.value = current
    }

    override suspend fun deleteFoodBlock(id: String) {
        val current = _blocksFlow.value.filter { it.id != id }
        _blocksFlow.value = current
    }
}
