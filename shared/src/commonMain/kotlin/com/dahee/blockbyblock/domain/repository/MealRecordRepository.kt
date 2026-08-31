package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.MealPreset
import kotlinx.coroutines.flow.Flow

interface MealRecordRepository {
    fun observeMealRecords(): Flow<List<DayMealRecord>>
    suspend fun getMealRecords(): List<DayMealRecord>
    suspend fun getMealRecordByDate(dateString: String): DayMealRecord?
    suspend fun saveMealRecord(record: DayMealRecord)
    suspend fun deleteMealRecord(id: String)

    fun observeMealPresets(): Flow<List<MealPreset>>
    suspend fun getMealPresets(): List<MealPreset>
    suspend fun saveMealPreset(preset: MealPreset)
    suspend fun deleteMealPreset(id: String)
}
