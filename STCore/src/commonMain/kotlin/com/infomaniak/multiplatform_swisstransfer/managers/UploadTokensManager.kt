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
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadTokensController
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.AttestationTokenException.InvalidAttestationTokenException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class UploadTokensManager(private val uploadTokensController: UploadTokensController) {

    //region Email token
    /**
     * Get a token for a given email.
     *
     * @param email The email possibly associated with a token.
     *
     * @return A token which may be associated with the email.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun getTokenForEmail(email: String): String? = withContext(Dispatchers.Default) {
        return@withContext uploadTokensController.getEmailTokenForEmail(email)?.token
    }

    /**
     * Asynchronously sets the email and it's corresponding token.
     *
     * @param email The validated email.
     * @param emailToken The valid token associated to the email.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     */
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setEmailToken(email: String, emailToken: String): Unit = withContext(Dispatchers.Default) {
        uploadTokensController.setEmailToken(email, emailToken)
    }
    //endregion

    //region Attestation token

    /**
     * Get the current attestation token.
     *
     * @return A token which is used to attest the app integrity to the API.
     *
     * @throws RealmException If an error occurs during database access.
     * @throws CancellationException If the operation is cancelled.
     * @throws InvalidAttestationTokenException If the token is expired of if we couldn't get a new one from the API
     */
    @Throws(RealmException::class, CancellationException::class, InvalidAttestationTokenException::class)
    suspend fun getAttestationToken(): String = withContext(Dispatchers.Default) {
        return@withContext uploadTokensController.getAttestationToken()?.token?.also(::assertAttestationTokenValidity)
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
        uploadTokensController.setAttestationToken(attestationToken)
    }

    private fun assertAttestationTokenValidity(attestationToken: String) {
        val tokenExpiryAt = runCatching {
            decodeJwtToken(attestationToken)!!
        }.getOrElse {
            throw InvalidAttestationTokenException("Invalid token", cause = it)
        }

        if (tokenExpiryAt < Clock.System.now().epochSeconds) {
            throw InvalidAttestationTokenException("Token expired")
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeJwtToken(token: String): Long? {
        val encodedPayload = token.split('.')[1]
        val decoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
        val payload = decoder.decode(encodedPayload.encodeToByteArray()).decodeToString()

        return Json.parseToJsonElement(payload).jsonObject[JSON_JWT_EXPIRATION_KEY]?.jsonPrimitive?.content?.toLong()
    }
    //endregion

    companion object {
        private const val JSON_JWT_EXPIRATION_KEY = "exp"
    }
}
