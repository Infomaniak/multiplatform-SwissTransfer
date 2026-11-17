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
package com.infomaniak.multiplatform_swisstransfer.managers

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.database.controllers.AttestationTokenStorage
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.UploadErrorsException.InvalidAttestationTokenException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AttestationTokenManager(private val attestationTokenStorage: AttestationTokenStorage) {

    /**
     * Get the current attestation token.
     *
     * @return A token which is used to attest the app integrity to the API.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     * @throws InvalidAttestationTokenException If the token is expired or if we couldn't get a new one from the API
     */
    @Throws(RealmException::class, CancellationException::class, InvalidAttestationTokenException::class)
    suspend fun getAttestationToken(): String = withContext(Dispatchers.Default) {
        return@withContext attestationTokenStorage.getAttestationToken()?.token?.also(::assertAttestationTokenValidity)
            ?: throw InvalidAttestationTokenException("Missing token")
    }

    /**
     * Asynchronously sets the attestation token.
     *
     * @param attestationToken The valid attestation token used to attest the app integrity to the API.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setAttestationToken(attestationToken: String): Unit = withContext(Dispatchers.Default) {
        attestationTokenStorage.setAttestationToken(attestationToken)
    }

    companion object {
        private const val JSON_JWT_EXPIRATION_KEY = "exp"

        @Throws(InvalidAttestationTokenException::class)
        internal fun assertAttestationTokenValidity(attestationToken: String) {
            val tokenExpiryAt = decodeJwtToken(attestationToken)
            @OptIn(ExperimentalTime::class)
            if (tokenExpiryAt < Clock.System.now().epochSeconds) throw InvalidAttestationTokenException("Local token is expired")
        }

        @Throws(InvalidAttestationTokenException::class)
        internal fun decodeJwtToken(token: String): Long = runCatching {
            val encodedPayload = token.split('.')[1]
            val decoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
            val payload = decoder.decode(encodedPayload.encodeToByteArray()).decodeToString()

            Json.parseToJsonElement(payload).jsonObject[JSON_JWT_EXPIRATION_KEY]?.jsonPrimitive?.content?.toLong()!!
        }.getOrElse {
            throw InvalidAttestationTokenException("Error decoding token", cause = it)
        }
    }
}
