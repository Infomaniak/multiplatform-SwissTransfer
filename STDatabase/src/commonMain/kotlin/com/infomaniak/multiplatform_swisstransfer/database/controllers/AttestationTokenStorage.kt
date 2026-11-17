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

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import com.infomaniak.multiplatform_swisstransfer.database.AppDatabase
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AttestationTokenDB as LegacyAttestationTokenDB
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.v2.AttestationTokenDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import com.infomaniak.multiplatform_swisstransfer.database.utils.findSuspend
import io.realm.kotlin.ext.query
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/**
 * Stores upload tokens in Room. Tokens still present in the legacy Realm database are
 * lazily migrated to Room when they're not found in Room, then removed from Realm.
 */
class AttestationTokenStorage(private val appDatabase: AppDatabase) {
    private val dao get() = appDatabase.attestationTokensDao

    @Throws(RealmException::class, CancellationException::class)
    suspend fun getAttestationToken(): AttestationTokenDB? = dao.getAttestationToken()

    @Throws(CancellationException::class)
    suspend fun setAttestationToken(token: String) {
        dao.upsertAttestationToken(AttestationTokenDB(token = token))
    }
}
