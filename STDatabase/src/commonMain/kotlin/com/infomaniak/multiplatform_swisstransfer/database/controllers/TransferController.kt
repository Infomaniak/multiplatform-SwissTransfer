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
import com.infomaniak.multiplatform_swisstransfer.common.interfaces.transfers.Transfer
import com.infomaniak.multiplatform_swisstransfer.common.models.TransferDirection
import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import com.infomaniak.multiplatform_swisstransfer.database.utils.RealmUtils.runThrowingRealm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.TRUE_PREDICATE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalCoroutinesApi::class)
class TransferController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmTransfers }

    //region Get data
    @Throws(RealmException::class)
    internal fun getTransfers(transferDirection: TransferDirection? = null): RealmResults<TransferDB>? = runThrowingRealm {
        val sentFilterQuery = when (transferDirection) {
            null -> TRUE_PREDICATE
            else -> "${TransferDB.transferDirectionPropertyName} == '${transferDirection}'"
        }
        return realm?.query<TransferDB>(sentFilterQuery)?.sort(TransferDB::createdDateTimestamp.name, Sort.DESCENDING)?.find()
    }

    @Throws(RealmException::class)
    fun getTransfersFlow(transferDirection: TransferDirection): Flow<List<Transfer>> = runThrowingRealm {
        return getTransfers(transferDirection)?.asFlow()?.mapLatest { it.list } ?: emptyFlow()
    }

    @Throws(RealmException::class)
    fun getTransfer(linkUUID: String): Transfer? = runThrowingRealm {
        return realm?.query<TransferDB>("${TransferDB::linkUUID.name} == '$linkUUID'")?.first()?.find()
    }
    //endregion

    //region Upsert data
    @Throws(RealmException::class, CancellationException::class)
    suspend fun upsert(transfer: Transfer, transferDirection: TransferDirection) = runThrowingRealm {
        realm?.write {
            this.copyToRealm(TransferDB(transfer, transferDirection), UpdatePolicy.ALL)
        }
    }
    //endregion

    //region Update data
    @Throws(RealmException::class, CancellationException::class)
    suspend fun removeData() = runThrowingRealm {
        realm?.write { deleteAll() }
    }
    //endregion
}
