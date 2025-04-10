/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.multiplatform_swisstransfer.network

import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiError
import com.infomaniak.multiplatform_swisstransfer.network.utils.getRequestContextId
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class ApiClientProvider internal constructor(
    engine: HttpClientEngine? = null,
    // When you don't use SwissTransferInjection, you don't have an userAgent, so we're currently setting a default value.
    // See later how to improve it.
    private val userAgent: String = "Ktor client",
) {

    constructor() : this(null)
    constructor(userAgent: String) : this(engine = null, userAgent)

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        useAlternativeNames = false
    }

    val httpClient = createHttpClient(engine)

    fun createHttpClient(engine: HttpClientEngine?): HttpClient {
        val block: HttpClientConfig<*>.() -> Unit = {
            install(UserAgent) {
                agent = userAgent
            }
            install(ContentNegotiation) {
                json(this@ApiClientProvider.json)
            }
            install(ContentEncoding) {
                gzip()
            }
            install(HttpTimeout) {
                // Each value can be fine-tuned independently, hence the value not being shared.
                requestTimeoutMillis = 10.seconds.inWholeMilliseconds
                connectTimeoutMillis = 10.seconds.inWholeMilliseconds
                socketTimeoutMillis = 10.seconds.inWholeMilliseconds
            }
            install(HttpRequestRetry) {
                retryOnExceptionIf(maxRetries = MAX_RETRY) { _, cause ->
                    cause.isNetworkException()
                }
                delayMillis { retry ->
                    retry * 500L
                }
            }
            HttpResponseValidator {
                validateResponse { response: HttpResponse ->
                    val requestContextId = response.getRequestContextId()
                    val statusCode = response.status.value
                    if (statusCode >= 300) {
                        val bodyResponse = response.bodyAsText()
                        val apiError = runCatching {
                            json.decodeFromString<ApiError>(bodyResponse)
                        }.getOrElse {
                            throw UnexpectedApiErrorFormatException(statusCode, bodyResponse, null, requestContextId)
                        }
                        throw ApiErrorException(apiError.errorCode, apiError.message, requestContextId)
                    }
                }
                handleResponseExceptionWithRequest { cause, request ->
                    when (cause) {
                        is IOException -> throw NetworkException("Network error: ${cause.message}")
                        is ApiException, is CancellationException -> throw cause
                        else -> {
                            val response = runCatching { request.call.response }.getOrNull()
                            val requestContextId = response?.getRequestContextId() ?: ""
                            val bodyResponse = response?.bodyAsText() ?: cause.message ?: ""
                            val statusCode = response?.status?.value ?: -1
                            throw UnexpectedApiErrorFormatException(statusCode, bodyResponse, cause, requestContextId)
                        }
                    }
                }
            }
        }

        return if (engine != null) HttpClient(engine, block) else HttpClient(block)
    }

    private fun Throwable.isNetworkException() = this is IOException

    companion object {
        private const val MAX_RETRY = 3
    }
}
