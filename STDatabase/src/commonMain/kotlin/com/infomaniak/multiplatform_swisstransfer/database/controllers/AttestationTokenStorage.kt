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
package com.infomaniak.multiplatform_swisstransfer.database.controllers


import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AttestationTokenDB
import kotlin.coroutines.cancellation.CancellationException

/**
 * Stores app integrity attestation tokens.
 */
class AttestationTokenStorage(private val appDatabase: AppDatabase) {
    private val dao get() = appDatabase.attestationTokensDao

    @Throws(CancellationException::class)
    suspend fun getAttestationToken(): AttestationTokenDB? = dao.getAttestationToken()

    @Throws(CancellationException::class)
    suspend fun setAttestationToken(token: String) {
        dao.upsertAttestationToken(AttestationTokenDB(token = token))
    }
}
