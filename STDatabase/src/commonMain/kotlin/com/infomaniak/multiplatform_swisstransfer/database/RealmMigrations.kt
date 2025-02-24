/*
 * Infomaniak SwissTransfer - Multiplatform
 * Copyright (C) 2025 Infomaniak Network SA
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
package com.infomaniak.multiplatform_swisstransfer.database

import io.realm.kotlin.dynamic.DynamicMutableRealmObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getValue
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.migration.AutomaticSchemaMigration.MigrationContext

val APP_SETTINGS_MIGRATION = AutomaticSchemaMigration { migrationContext ->

}

val UPLOAD_MIGRATION = AutomaticSchemaMigration { migrationContext ->

}

val TRANSFERS_MIGRATION = AutomaticSchemaMigration { migrationContext ->
    migrationContext.renameEnumValueAfterFirstMigration()
}

// Migrate from version #1
private fun MigrationContext.renameEnumValueAfterFirstMigration() {

    if (oldRealm.schemaVersion() <= 1L) {

        enumerate(className = "TransferDB") { oldObject: DynamicRealmObject, newObject: DynamicMutableRealmObject? ->
            newObject?.apply {
                val transferStatusValue = oldObject.getValue<String>("transferStatusValue")
                if (transferStatusValue == "EXPIRED") {
                    set(propertyName = "transferStatusValue", value = "EXPIRED_DATE")
                }
            }
        }

    }
}
