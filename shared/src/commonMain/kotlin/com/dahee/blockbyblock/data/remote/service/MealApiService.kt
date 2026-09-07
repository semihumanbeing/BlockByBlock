package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.CreateMealPresetRequest
import com.dahee.blockbyblock.data.remote.dto.DailyMealResponse
import com.dahee.blockbyblock.data.remote.dto.MealPresetResponse
import com.dahee.blockbyblock.data.remote.dto.MealSlotResponse
import com.dahee.blockbyblock.data.remote.dto.SaveMealSlotRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateMealPresetRequest
import com.dahee.blockbyblock.data.remote.dto.WeeklyMealResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class MealApiService {

    suspend fun getDailyMeal(dateString: String): Result<DailyMealResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/meals")) {
            parameter("date", dateString)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<DailyMealResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun getWeeklyMeals(weekStartDate: String): Result<WeeklyMealResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/meals/weekly")) {
            parameter("startDate", weekStartDate)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<WeeklyMealResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun saveMealSlot(date: String, mealType: String, request: SaveMealSlotRequest): Result<MealSlotResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.put(ApiClient.endpoint("api/v1/meals/$date/$mealType")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MealSlotResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteMealSlot(date: String, mealType: String): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/meals/$date/$mealType"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    suspend fun getMealPresets(): Result<List<MealPresetResponse>> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/meal-presets"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<List<MealPresetResponse>>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun createMealPreset(request: CreateMealPresetRequest): Result<MealPresetResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/meal-presets")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MealPresetResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateMealPreset(presetId: Long, request: UpdateMealPresetRequest): Result<MealPresetResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/meal-presets/$presetId")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MealPresetResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteMealPreset(presetId: Long): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/meal-presets/$presetId"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    private suspend fun parseError(response: HttpResponse): Exception {
        val text = response.bodyAsText()
        val message = try {
            val err = ApiClient.jsonConfig.decodeFromString<ApiErrorResponse>(text)
            err.message ?: err.error ?: "Request failed (${response.status.value})"
        } catch (_: Exception) {
            "Request failed (${response.status.value}): $text"
        }
        return Exception(message)
    }
}
