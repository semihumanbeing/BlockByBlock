package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.CatalogIngredientResponse
import com.dahee.blockbyblock.data.remote.dto.CatalogPageResponse
import com.dahee.blockbyblock.data.remote.dto.CreateIngredientRequest
import com.dahee.blockbyblock.data.remote.dto.IngredientListResponse
import com.dahee.blockbyblock.data.remote.dto.IngredientResponse
import com.dahee.blockbyblock.data.remote.dto.UpdateIngredientRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateIngredientStatusRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class IngredientApiService {

    suspend fun getCatalogIngredients(
        query: String? = null,
        category: String? = null,
        lang: String? = "KO",
        page: Int = 1,
        size: Int = 20
    ): Result<CatalogPageResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/catalog/ingredients")) {
            if (!query.isNullOrBlank()) {
                parameter("query", query)
            }
            if (!category.isNullOrBlank()) {
                parameter("category", category)
            }
            if (!lang.isNullOrBlank()) {
                parameter("lang", lang)
            }
            parameter("page", page)
            parameter("size", size)
        }
        if (response.status.isSuccess()) {
            val rawText = response.bodyAsText()
            val jsonElement = ApiClient.jsonConfig.parseToJsonElement(rawText)
            val dataElement = (jsonElement as? kotlinx.serialization.json.JsonObject)?.get("data")
            when (dataElement) {
                is kotlinx.serialization.json.JsonObject -> {
                    ApiClient.jsonConfig.decodeFromJsonElement(CatalogPageResponse.serializer(), dataElement)
                }
                is kotlinx.serialization.json.JsonArray -> {
                    val list = ApiClient.jsonConfig.decodeFromJsonElement(
                        kotlinx.serialization.builtins.ListSerializer(CatalogIngredientResponse.serializer()),
                        dataElement
                    )
                    CatalogPageResponse(
                        items = list,
                        page = 1,
                        size = list.size.coerceAtLeast(1),
                        totalElements = list.size.toLong(),
                        totalPages = 1,
                        hasNext = false,
                        hasPrevious = false
                    )
                }
                else -> CatalogPageResponse()
            }
        } else {
            throw parseError(response)
        }
    }

    suspend fun getIngredients(
        page: Int = 1,
        size: Int = 12,
        status: String? = null,
        category: String? = null,
        query: String? = null
    ): Result<IngredientListResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/ingredients")) {
            parameter("page", page)
            parameter("size", size)
            if (!status.isNullOrBlank()) {
                parameter("status", status)
            }
            if (!category.isNullOrBlank()) {
                parameter("category", category)
            }
            if (!query.isNullOrBlank()) {
                parameter("query", query)
            }
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<IngredientListResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun createIngredient(request: CreateIngredientRequest): Result<IngredientResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/ingredients")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<IngredientResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateIngredient(ingredientId: Long, request: UpdateIngredientRequest): Result<IngredientResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/ingredients/$ingredientId")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<IngredientResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateStatus(ingredientId: Long, status: String): Result<IngredientResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/ingredients/$ingredientId/status")) {
            setBody(UpdateIngredientStatusRequest(status = status))
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<IngredientResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteIngredient(ingredientId: Long): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/ingredients/$ingredientId"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    suspend fun restoreIngredient(ingredientId: Long): Result<IngredientResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/ingredients/$ingredientId/restore"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<IngredientResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    private suspend fun parseError(response: HttpResponse): com.dahee.blockbyblock.data.remote.error.ApiError {
        return ApiClient.parseError(response)
    }
}
