/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2026 Infomaniak Network SA
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

import io.realm.kotlin.internal.interop.ErrorCode

/** Ignore all errors due to voluntary Realm closure
 * @return true if the error is recognized, otherwise false
 **/
fun Throwable.isExpectedRealmError(): Boolean = message?.run {
    contains(ErrorCode.RLM_ERR_CLOSED_REALM.name)
            || contains(ErrorCode.RLM_ERR_INVALIDATED_OBJECT.name)
            || contains(ErrorCode.RLM_ERR_INVALID_TABLE_REF.name)
            || contains(ErrorCode.RLM_ERR_STALE_ACCESSOR.name)
} ?: false
