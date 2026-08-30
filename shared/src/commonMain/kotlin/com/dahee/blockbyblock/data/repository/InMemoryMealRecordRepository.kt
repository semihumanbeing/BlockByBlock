package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.repository.MealRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryMealRecordRepository : MealRecordRepository {
    private val _records = MutableStateFlow<List<DayMealRecord>>(emptyList())

    override fun observeMealRecords(): Flow<List<DayMealRecord>> = _records.asStateFlow()

    override suspend fun getMealRecords(): List<DayMealRecord> = _records.value

    override suspend fun getMealRecordByDate(dateString: String): DayMealRecord? {
        return _records.value.find { it.dateString == dateString }
    }

    override suspend fun saveMealRecord(record: DayMealRecord) {
        val current = _records.value.toMutableList()
        val index = current.indexOfFirst { it.id == record.id || it.dateString == record.dateString }
        if (index >= 0) {
            current[index] = record
        } else {
            current.add(0, record)
        }
        _records.value = current
    }

    override suspend fun deleteMealRecord(id: String) {
        _records.value = _records.value.filter { it.id != id }
    }
}
