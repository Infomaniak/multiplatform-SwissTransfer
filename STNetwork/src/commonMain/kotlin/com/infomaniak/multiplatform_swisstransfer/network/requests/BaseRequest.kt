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
package com.infomaniak.multiplatform_swisstransfer.network.requests

import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import com.infomaniak.multiplatform_swisstransfer.network.utils.decode
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

internal open class BaseRequest(
    protected val environment: ApiEnvironment,
    protected val json: Json,
    protected val httpClient: HttpClient,
    private val token: () -> String,
) {

    protected fun createUrl(path: String, vararg queries: Pair<String, String>): Url {
        return createUrl(ApiRoutes.apiBaseUrl(environment), path, queries)
    }

    protected fun createV2Url(path: String, vararg queries: Pair<String, String>): Url {
        return createUrl(ApiRoutes.apiBaseUrlV2(environment), path, queries)
    }

    private fun createUrl(
        baseUrl: String,
        path: String,
        queries: Array<out Pair<String, String>>
    ): Url = URLBuilder(Url(baseUrl + path)).apply {
        queries.forEach { parameters.append(it.first, it.second) }
    }.build()

    protected fun HeadersBuilder.appendBearer() {
        append(HttpHeaders.Authorization, "Bearer ${token()}")
    }

    protected suspend inline fun <reified R> get(
        url: Url,
        crossinline appendHeaders: HeadersBuilder.() -> Unit = {},
        httpClient: HttpClient = this.httpClient,
    ): R {
        return httpClient.get(url) {
            headers { appendHeaders() }
        }.body<R>()
    }

    protected suspend inline fun <reified R> post(
        url: Url, data: Any?,
        crossinline appendHeaders: HeadersBuilder.() -> Unit = {},
        httpClient: HttpClient = this.httpClient,
    ): R {
        return httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { appendHeaders() }
            setBody(data)
        }.decode<R>()
    }

    protected suspend inline fun <reified R> put(url: Url, data: Any?, httpClient: HttpClient = this.httpClient): R {
        return httpClient.put(url) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.decode<R>()
    }

    protected suspend inline fun <reified R> patch(url: Url, data: Any?, httpClient: HttpClient = this.httpClient): R {
        return httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            setBody(data)
        }.decode<R>()
    }

    protected suspend inline fun <reified R> delete(url: Url, httpClient: HttpClient = this.httpClient): R {
        return httpClient.delete(url) {}.decode<R>()
    }
}
