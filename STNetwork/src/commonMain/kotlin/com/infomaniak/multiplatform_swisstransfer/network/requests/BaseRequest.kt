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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import kotlinx.serialization.json.Json

internal open class BaseRequest(protected val json: Json, protected val httpClient: HttpClient) {

    protected fun createUrl(path: String, vararg queries: Pair<String, String>): Url {
        val baseUrl = Url(ApiRoutes.API_BASE_URL + path)
        return URLBuilder(baseUrl).apply {
            queries.forEach { parameters.append(it.first, it.second) }
        }.build()
    }

    protected suspend inline fun <reified R> get(
        url: Url,
        crossinline appendHeaders: HeadersBuilder.() -> Unit = {},
        httpClient: HttpClient = this.httpClient,
    ): R {
        return httpClient.get(url) {
            headers { appendHeaders() }
        }.decode<R>()
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

    protected suspend inline fun <reified R> delete(url: Url, httpClient: HttpClient = this.httpClient): R {
        return httpClient.delete(url) {}.decode<R>()
    }

    protected suspend inline fun <reified R> HttpResponse.decode(): R {
        return runCatching {
            body<R>()
        }.getOrElse { exception ->
            when (exception) {
                is ApiException, is NetworkException, is UnexpectedApiErrorFormatException -> throw exception
                else -> throw UnknownException(exception)
            }
        }
    }
}
