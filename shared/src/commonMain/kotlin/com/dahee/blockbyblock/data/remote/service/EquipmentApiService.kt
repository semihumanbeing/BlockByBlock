package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.CreateMoldRequest
import com.dahee.blockbyblock.data.remote.dto.EquipmentsResponse
import com.dahee.blockbyblock.data.remote.dto.MoldResponse
import com.dahee.blockbyblock.data.remote.dto.SyncEquipmentsRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateMoldQuantityRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateMoldRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class EquipmentApiService {

    suspend fun getEquipments(): Result<EquipmentsResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/equipments"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<EquipmentsResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun syncEquipments(request: SyncEquipmentsRequest): Result<EquipmentsResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.put(ApiClient.endpoint("api/v1/equipments")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<EquipmentsResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun createMold(request: CreateMoldRequest): Result<MoldResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/molds")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MoldResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateMold(moldId: Long, request: UpdateMoldRequest): Result<MoldResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/molds/$moldId")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MoldResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateMoldQuantity(moldId: Long, quantity: Int): Result<MoldResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/molds/$moldId/quantity")) {
            setBody(UpdateMoldQuantityRequest(quantity = quantity))
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<MoldResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteMold(moldId: Long): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/molds/$moldId"))
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
