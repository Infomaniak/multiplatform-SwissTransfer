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
package com.infomaniak.multiplatform_swisstransfer.database.utils

import com.infomaniak.multiplatform_swisstransfer.common.exceptions.RealmException
import io.realm.kotlin.query.RealmElementQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.RealmSingleQuery
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object RealmUtils {

    @Throws(RealmException::class)
    inline fun <R> runThrowingRealm(block: () -> R): R {
        return runCatching { block() }.onFailure {
            if (it is CancellationException) throw it
        }.getOrElse { throw RealmException(it) }
    }
}

suspend fun <T : BaseRealmObject> RealmElementQuery<T>.findSuspend(): RealmResults<T> {
    return asFlow().map { it.list }.first()
}

suspend fun <T : BaseRealmObject> RealmSingleQuery<T>.findSuspend(): T? {
    return asFlow().map { it.obj }.first()
}
