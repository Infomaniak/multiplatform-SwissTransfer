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
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnknownApiException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiError
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.json.Json

class ApiClientProvider {

    val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        useAlternativeNames = false
    }

    fun createHttpClient(engine: HttpClientEngineFactory<*>? = null): HttpClient {
        val block: HttpClientConfig<*>.() -> Unit = {
            install(ContentNegotiation) {
                json(this@ApiClientProvider.json)
            }
            install(ContentEncoding) {
                gzip()
            }
            install(HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT
                connectTimeoutMillis = REQUEST_TIMEOUT
                socketTimeoutMillis = REQUEST_TIMEOUT
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
                    val errorCode = response.status.value
                    val bodyResponse = response.bodyAsText()
                    if (errorCode >= 300) {
                        runCatching {
                            val apiError = json.decodeFromString<ApiError>(bodyResponse)
                            throw ApiException(apiError.errorCode, apiError.message)
                        }.onFailure {
                            throw UnknownApiException(errorCode, bodyResponse)
                        }
                    }
                }
                handleResponseExceptionWithRequest { cause, _ ->
                    when (cause) {
                        is IOException -> throw NetworkException("Network error: ${cause.message}")
                        else -> throw cause
                    }
                }
            }
        }

        return if (engine != null) HttpClient(engine, block) else HttpClient(block)
    }

    private fun Throwable.isNetworkException() = this is IOException

    private companion object {
        const val REQUEST_TIMEOUT = 10_000L
        const val MAX_RETRY = 3
    }
}
