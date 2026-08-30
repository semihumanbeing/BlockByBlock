package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.DayMealRecord
import kotlinx.coroutines.flow.Flow

interface MealRecordRepository {
    fun observeMealRecords(): Flow<List<DayMealRecord>>
    suspend fun getMealRecords(): List<DayMealRecord>
    suspend fun getMealRecordByDate(dateString: String): DayMealRecord?
    suspend fun saveMealRecord(record: DayMealRecord)
    suspend fun deleteMealRecord(id: String)
}
