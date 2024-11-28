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
package com.infomaniak.multiplatform_swisstransfer.database

import com.infomaniak.multiplatform_swisstransfer.common.models.EmailLanguage
import com.infomaniak.multiplatform_swisstransfer.database.controllers.UploadController
import com.infomaniak.multiplatform_swisstransfer.database.dataset.DummyUpload
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class UploadControllerTest {

    private lateinit var realmProvider: RealmProvider
    private lateinit var uploadController: UploadController

    @BeforeTest
    fun setup() {
        realmProvider = RealmProvider(loadDataInMemory = true)
        uploadController = UploadController(realmProvider)
    }

    @AfterTest
    fun tearDown() = runTest {
        uploadController.removeData()
        realmProvider.closeRealmUploads()
    }

    @Test
    fun canCreateAndGetAnUpload() = runTest {
        val dummyUpload = DummyUpload.uploads.first()
        val realmUpload = uploadController.insertAndGet(dummyUpload)
        assertNotNull(realmUpload)
        assertEquals(realmUpload.language, EmailLanguage.ITALIAN)
    }

    @Test
    fun canGetUploads() = runTest {
        DummyUpload.uploads.take(2).forEach { dummyUpload ->
            uploadController.insertAndGet(dummyUpload)
        }
        val realmUploads = uploadController.getAllUploads()
        assertEquals(2, realmUploads.count())
    }

    @Test
    fun canRemoveData() = runTest {
        uploadController.insertAndGet(DummyUpload.uploads.first())
        uploadController.removeData()
        val realmUploads = uploadController.getAllUploads()
        assertEquals(0, realmUploads.count())
    }

    @Test
    fun cannotInsertExistingUpload() = runTest {
        val dummyUpload = DummyUpload.uploads.first()
        uploadController.insertAndGet(dummyUpload)
        assertFails { uploadController.insertAndGet(dummyUpload) }
    }

    @Test
    fun canUpdateSessionContainer() = runTest {
        val dummyContainer = DummyUpload.container
        val dummyUpload = DummyUpload.uploads.first()
        uploadController.insertAndGet(dummyUpload)

        val realmUpload1 = uploadController.getUploadByUUID(dummyUpload.uuid)
        assertNotNull(realmUpload1)
        assertNull(realmUpload1.remoteContainer)

        uploadController.updateUploadSession(
            uuid = dummyUpload.uuid,
            remoteContainer = dummyContainer,
            remoteUploadHost = "remoteHost",
            remoteFilesUUID = listOf("dhsd"),
        )
        val realmUpload2 = uploadController.getUploadByUUID(dummyUpload.uuid)
        assertNotNull(realmUpload2)
        assertNotNull(realmUpload2.remoteContainer)
    }

}
