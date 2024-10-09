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
package com.infomaniak.multiplatform_swisstransfer.database.cache.setting

import com.infomaniak.multiplatform_swisstransfer.database.RealmProvider
import com.infomaniak.multiplatform_swisstransfer.database.models.transfers.TransferDB
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlin.coroutines.cancellation.CancellationException

class TransfersController(private val realmProvider: RealmProvider) {

    private val realm by lazy { realmProvider.realmTransfers }

    //region Get data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    fun getTransfers(): RealmResults<TransferDB>? = realm?.query<TransferDB>()?.find()
    //endregion

    //region Update data
    @Throws(IllegalArgumentException::class, CancellationException::class)
    suspend fun removeData() {
        realm?.write { deleteAll() }
    }
    //endregion
}
