/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
import com.infomaniak.multiplatform_swisstransfer.network.models.ApiError
import com.infomaniak.multiplatform_swisstransfer.network.utils.CONTENT_REQUEST_ID_HEADER
import com.infomaniak.multiplatform_swisstransfer.network.utils.decode
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.expect

class ApiClientProviderTest {

    private val unexpectedFormatExceptionApiClientProvider by lazy {
        ApiClientProvider(createMockEngine("""{"ip":"127.0.0.1"}"""), userAgent = TEST_USER_AGENT)
    }

    private val apiErrorExceptionApiClientProvider by lazy {
        ApiClientProvider(createMockEngine(Json.encodeToString(ApiError(errorCode = 422, message = ""))), TEST_USER_AGENT)
    }

    @Test
    fun requestContentIdTest_unexpectedApiErrorFormatException() {
        expect(TEST_CONTENT_REQUEST_ID) {
            runCatching {
                runBlocking {
                    post<Any>(apiClientProvider = unexpectedFormatExceptionApiClientProvider, url = Url(""), data = null)
                }
            }.getOrElse {
                if (it is ApiException.UnexpectedApiErrorFormatException) it.requestContextId else null
            }
        }
    }

    @Test
    fun requestContentIdTest_apiErrorException() {
        expect(TEST_CONTENT_REQUEST_ID) {
            runCatching {
                runBlocking {
                    post<Any>(apiClientProvider = apiErrorExceptionApiClientProvider, url = Url(""), data = null)
                }
            }.getOrElse {
                if (it is ApiException.ApiErrorException) it.requestContextId else null
            }
        }
    }

    private suspend inline fun <reified R> post(apiClientProvider: ApiClientProvider, url: Url, data: Any?): R {
        return apiClientProvider.httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.decode<R>()
    }

    private fun createMockEngine(content: String) = MockEngine { _ ->
        respond(
            content = content,
            status = HttpStatusCode.UnprocessableEntity,
            headers = headersOf(
                HttpHeaders.ContentType to listOf("application/json"),
                CONTENT_REQUEST_ID_HEADER to listOf(TEST_CONTENT_REQUEST_ID),
            ),
        )
    }

    companion object {

        private const val TEST_USER_AGENT = "Ktor client test"
        private const val TEST_CONTENT_REQUEST_ID = "test_request_context_id"
    }
}

