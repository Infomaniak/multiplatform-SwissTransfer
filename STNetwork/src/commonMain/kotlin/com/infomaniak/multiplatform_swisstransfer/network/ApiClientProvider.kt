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

import com.infomaniak.multiplatform_swisstransfer.common.interfaces.BreadcrumbType
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportInterface
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.CrashReportLevel
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiError
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiResponseForError
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
import io.ktor.client.statement.request
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.network.UnresolvedAddressException
import io.ktor.utils.io.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class ApiClientProvider internal constructor(
    engine: HttpClientEngine? = null,
    // When you don't use SwissTransferInjection, you don't have an userAgent, so we're currently setting a default value.
    // See later how to improve it.
    private val userAgent: String = "Ktor client",
    private val crashReport: CrashReportInterface? = null,
) {

    constructor() : this(null)
    constructor(userAgent: String, crashReport: CrashReportInterface) : this(
        engine = null,
        userAgent = userAgent,
        crashReport = crashReport,
    )

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

                    addSentryUrlBreadcrumb(response, statusCode, requestContextId)

                    if (statusCode >= 300) {
                        val bodyResponse = response.bodyAsText()
                        runCatching {
                            if (bodyResponse.isFromApiV2()) {
                                val error = json.decodeFromString<ApiResponseForError>(bodyResponse).error
                                throw ApiV2ErrorException(error.code, error.description, requestContextId)
                            } else {
                                val error = json.decodeFromString<ApiError>(bodyResponse)
                                throw ApiErrorException(error.errorCode, error.message, requestContextId)
                            }
                        }.getOrElse {
                            throw UnexpectedApiErrorFormatException(statusCode, bodyResponse, null, requestContextId)
                        }
                    }
                }
                handleResponseExceptionWithRequest { cause, request ->
                    when (cause) {
                        is UnresolvedAddressException,
                        is IOException -> throw NetworkException("Network error: ${cause.message}", cause)
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

    private fun addSentryUrlBreadcrumb(response: HttpResponse, statusCode: Int, requestContextId: String) {
        val requestUrl = response.request.url
        val data = buildMap {
            put("url", "${requestUrl.protocol.name}://${requestUrl.host}${requestUrl.encodedPath}")
            put("method", response.request.method.value)
            put("status_code", "$statusCode")
            if (requestUrl.encodedQuery.isNotEmpty()) put("http.query", requestUrl.encodedQuery)
            put("request_id", requestContextId)
            put("http.start_timestamp", "${response.requestTime.timestamp}")
            put("http.end_timestamp", "${response.responseTime.timestamp}")
            response.contentLength()?.let { put("response_content_length", "$it") }
        }
        crashReport?.addBreadcrumb(
            message = "",
            category = "http",
            level = CrashReportLevel.INFO,
            type = BreadcrumbType.HTTP,
            data = data
        )
    }

    private fun Throwable.isNetworkException() = this is IOException

    companion object {
        private const val MAX_RETRY = 3

        //TODO[API-V2]: Delete once the v1 api has been removed
        private fun String.isFromApiV2() = contains("\"result\": \"error\"")
    }
}
