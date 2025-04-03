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
package com.infomaniak.multiplatform_swisstransfer.network.utils

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.CancellationException
import kotlin.time.Duration.Companion.hours

const val CONTENT_REQUEST_ID_HEADER = "x-request-id"

internal fun HttpRequestBuilder.longTimeout() {
    timeout {
        requestTimeoutMillis = 1.hours.inWholeMilliseconds // Be permissive for DSL or slow mobile connections.
        connectTimeoutMillis = ApiClientProvider.REQUEST_TIMEOUT // Don't let the user wait too long for connection.
        socketTimeoutMillis = ApiClientProvider.REQUEST_LONG_TIMEOUT
    }
}

internal fun HttpResponse.getRequestContextId() = headers[CONTENT_REQUEST_ID_HEADER] ?: ""

internal suspend inline fun <reified R> HttpResponse.decode(): R = runCatching {
    body<R>()
}.getOrElse { exception ->
    when (exception) {
        is CancellationException, is NetworkException, is ApiException -> throw exception
        else -> throw UnknownException(exception)
    }
}
