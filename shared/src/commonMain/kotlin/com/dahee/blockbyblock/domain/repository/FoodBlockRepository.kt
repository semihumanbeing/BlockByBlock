package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.FoodBlock
import kotlinx.coroutines.flow.Flow

interface FoodBlockRepository {
    fun observeFoodBlocks(): Flow<List<FoodBlock>>
    suspend fun getFoodBlocks(): List<FoodBlock>
    suspend fun saveFoodBlock(block: FoodBlock)
    suspend fun deleteFoodBlock(id: String)
}
