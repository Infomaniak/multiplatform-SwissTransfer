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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.EmailToken
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.appSettings.UploadToken
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.AttestationTokenDB
import com.infomaniak.multiplatform_swisstransfer.database.models.appSettings.EmailTokenDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlin.coroutines.cancellation.CancellationException

class UploadTokensController(private val realmProvider: RealmProvider) {
    private val realm by lazy { realmProvider.appSettings }

    //region Get data
    @Throws(RealmException::class)
    fun getEmailTokenForEmail(email: String): EmailToken? = runThrowingRealm {
        val query = "${EmailTokenDB::email.name} == '$email'"
        return realm.query<EmailTokenDB>(query).first().find()
    }

    @Throws(RealmException::class)
    fun getAttestationToken(): UploadToken? = runThrowingRealm {
        return realm.query<AttestationTokenDB>().first().find()
    }
    //endregion

    //region Update data
    @Throws(RealmException::class, CancellationException::class)
    suspend fun setEmailToken(email: String, token: String) = runThrowingRealm {
        realm.write {
            val emailTokenDB = EmailTokenDB(email, token)
            copyToRealm(emailTokenDB, UpdatePolicy.ALL)
        }
    }

    @Throws(RealmException::class, CancellationException::class)
    suspend fun setAttestationToken(token: String) = runThrowingRealm {
        realm.write {
            deleteAll() // Always only keep the last token
            copyToRealm(AttestationTokenDB(token), UpdatePolicy.ALL)
        }
    }
    //endregion
}
