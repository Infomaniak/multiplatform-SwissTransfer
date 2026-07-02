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
package com.infomaniak.multiplatform_swisstransfer.network.repositories

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.UnknownException
import com.infomaniak.multiplatform_swisstransfer.common.utils.ApiEnvironment
import com.infomaniak.multiplatform_swisstransfer.network.ApiClientProvider
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.ApiV2ErrorException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.ApiException.UnexpectedApiErrorFormatException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.NetworkException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.TooManyRequestException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UnauthorizedException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.NotFoundException
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.toUploadErrorsException
import com.infomaniak.multiplatform_swisstransfer.network.models.MyUser
import com.infomaniak.multiplatform_swisstransfer.network.requests.v2.UserInfoRequests
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

class UserInfoRepository internal constructor(private val userInfoRequests: UserInfoRequests) {

    constructor(
        apiClientProvider: ApiClientProvider = ApiClientProvider(),
        environment: ApiEnvironment,
        token: () -> String,
    ) : this(
        environment = environment,
        json = apiClientProvider.json,
        httpClient = apiClientProvider.httpClient,
        token = token,
    )

    internal constructor(
        environment: ApiEnvironment,
        json: Json,
        httpClient: HttpClient,
        token: () -> String,
    ) : this(UserInfoRequests(environment, json, httpClient, token))

    @Throws(
        CancellationException::class,
        NetworkException::class,
        ApiV2ErrorException::class,
        UnexpectedApiErrorFormatException::class,
        UnknownException::class,
        UnauthorizedException::class,
        TooManyRequestException::class,
        NotFoundException::class,
    )
    suspend fun getMyUserInfo(): MyUser = withErrorHandling {
        userInfoRequests.getMyUserInfo().data
    }

    private suspend fun <T> withErrorHandling(block: suspend () -> T) = runCatching {
        block()
    }.getOrElse { exception ->
        throw when (exception) {
            is ApiV2ErrorException if exception.code == "not_authorized" -> UnauthorizedException(exception.requestContextId)
            is ApiV2ErrorException if exception.code == "too_many_request" -> TooManyRequestException(exception.requestContextId)
            is ApiV2ErrorException -> exception.toUploadErrorsException()
            else -> exception
        }
    }
}
