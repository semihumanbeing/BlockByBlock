package com.dahee.blockbyblock.data.remote.service

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.ApiErrorResponse
import com.dahee.blockbyblock.data.remote.ApiResponse
import com.dahee.blockbyblock.data.remote.dto.BlockHistoryResponse
import com.dahee.blockbyblock.data.remote.dto.BlockQuantityResponse
import com.dahee.blockbyblock.data.remote.dto.BlockResponse
import com.dahee.blockbyblock.data.remote.dto.CreateBlockRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateBlockQuantityRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateBlockRequest
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

class BlockApiService {

    suspend fun getBlocks(): Result<List<BlockResponse>> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/blocks"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<List<BlockResponse>>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun createBlock(request: CreateBlockRequest): Result<BlockResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.post(ApiClient.endpoint("api/v1/blocks")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<BlockResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun getBlock(blockId: Long): Result<BlockResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/blocks/$blockId"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<BlockResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateBlock(blockId: Long, request: UpdateBlockRequest): Result<BlockResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.put(ApiClient.endpoint("api/v1/blocks/$blockId")) {
            setBody(request)
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<BlockResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun deleteBlock(blockId: Long): Result<Boolean> = runCatching {
        val response: HttpResponse = ApiClient.client.delete(ApiClient.endpoint("api/v1/blocks/$blockId"))
        if (response.status.isSuccess()) {
            true
        } else {
            throw parseError(response)
        }
    }

    suspend fun updateQuantity(blockId: Long, quantity: Int? = null, delta: Int? = null): Result<BlockQuantityResponse> = runCatching {
        val response: HttpResponse = ApiClient.client.patch(ApiClient.endpoint("api/v1/blocks/$blockId/quantity")) {
            setBody(UpdateBlockQuantityRequest(quantity = quantity, delta = delta))
        }
        if (response.status.isSuccess()) {
            response.body<ApiResponse<BlockQuantityResponse>>().data
        } else {
            throw parseError(response)
        }
    }

    suspend fun getHistory(): Result<List<BlockHistoryResponse>> = runCatching {
        val response: HttpResponse = ApiClient.client.get(ApiClient.endpoint("api/v1/blocks/history"))
        if (response.status.isSuccess()) {
            response.body<ApiResponse<List<BlockHistoryResponse>>>().data
        } else {
            throw parseError(response)
        }
    }

    private suspend fun parseError(response: HttpResponse): com.dahee.blockbyblock.data.remote.error.ApiError {
        return ApiClient.parseError(response)
    }
}
