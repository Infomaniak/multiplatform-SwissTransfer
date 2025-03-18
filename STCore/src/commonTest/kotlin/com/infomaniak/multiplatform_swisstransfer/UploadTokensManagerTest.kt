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
package com.infomaniak.multiplatform_swisstransfer

import com.infomaniak.multiplatform_swisstransfer.managers.UploadTokensManager
import com.infomaniak.multiplatform_swisstransfer.network.exceptions.AttestationTokenException.InvalidAttestationTokenException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UploadTokensManagerTest {

    @Test
    fun decodeJwtToken_EmptyTokenTest() {
        assertFailsWith<InvalidAttestationTokenException> { UploadTokensManager.decodeJwtToken("") }
    }

    @Test
    fun decodeJwtToken_InvalidTokenTest() {
        val missingHeaderPartJWT = FUTURE_TOKEN.drop(64)
        val dummyJWTFormat = "a.a.a"
        assertFailsWith<InvalidAttestationTokenException> { UploadTokensManager.decodeJwtToken(missingHeaderPartJWT) }
        assertFailsWith<InvalidAttestationTokenException> { UploadTokensManager.decodeJwtToken(dummyJWTFormat) }
    }

    @Test
    fun decodeJwtToken_ValidTokenTest() {
        assertIs<Long>(UploadTokensManager.decodeJwtToken(EXPIRED_TOKEN))
        assertIs<Long>(UploadTokensManager.decodeJwtToken(FUTURE_TOKEN))
    }

    @Test
    fun assertAttestationTokenValidity_ExpiredTokenTest() {
        assertFailsWith<InvalidAttestationTokenException> { UploadTokensManager.assertAttestationTokenValidity("") }
        assertFailsWith<InvalidAttestationTokenException> { UploadTokensManager.assertAttestationTokenValidity(EXPIRED_TOKEN) }
    }

    @Test
    fun assertAttestationTokenValidity_ValidTokenTest() {
        UploadTokensManager.assertAttestationTokenValidity(FUTURE_TOKEN)
    }

    companion object {

        private const val EXPIRED_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImFwaS1jb3JlLXYxIn0.eyJqdGkiOiIyNWMyM2ZkNy04ODEzLTQ0MDMtYjIwOS00ZDAxODU3YWZmNTQiLCJpc3MiOiJodHRwczovL2FwaS5wcmVwcm9kLmRldi5pbmZvbWFuaWFrLmNoLzEvYXR0ZXN0Iiwic3ViIjoiY29tLmluZm9tYW5pYWsuc3dpc3N0cmFuc2ZlciIsImF1ZCI6Imh0dHBzOi8vc3dpc3N0cmFuc2Zlci5wcmVwcm9kLmRldi5pbmZvbWFuaWFrLmNoL2FwaS9tb2JpbGUvY29udGFpbmVycyIsImV4cCI6MTc0MTg3NjEwNiwiaWF0IjoxNzQxNzg5NzA2fQ.gNF8xcnXgm_ozjQoRQxmWzA6n7N3v-vsMp6QnFVPSwj4_VyHwwH1UeYRAzzvh-HZWe-DSQxgUr6plYzcrk4Xi8DVjEtigYHjo8M048Mf9qIxn7GOKnBscAg7-J6H-ss1Nc57DzOF_6mJdB302SfoWerKmB3Jorw-T6iT7zeEpPtiEMUHaJHxy-tiqMQp3bBHRp5yZRueT_dyy_by8s9n3n_6hTnw5lEjHRkwxX9Uyb7DJKnRcrytr6JO6tVMfDlrbJjHwcwOtafYWwJL6l_SrXUIy_1GK7Hb4kYMgGiAkaMGOxB2-2oZZYmCT1MSOrd88lDNgEDb_BOucEGWubkAH2yIgui--DjrTJBj4NhBZUhj4Vw7zCwOQvZkAa_4lXWBWwAeGKqicWk5UOgCXkcaN7TFj3v-FYXa8y_gRcoPb5dxykIF0lrsBmeYPtRHftUNEWWVx0aDfw91DJ5MLEnrRzyMGEoNXkJidWr6CgXwO2MdVuEsJtQ1_jOixo80i7tDRZArtkDGYrE3ZnP7lwCCzk91jiFaaYjPH-DNBnukMqEgppwDVuv0c4tQYkT4D4ktMoN15LQg3zZgH6uR4P1kCm1HNUvc_pY1f8ZzRq5JQCJgIwT3YbpndxDQh5i4v7aRK0XzHdk9L_1i6GgIYWebIODPmWJ-DydrwiSmMfo5MzQ"

        // Expires in ~500 years
        private const val FUTURE_TOKEN =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImFwaS1jb3JlLXYxIn0.eyJqdGkiOiIyNWMyM2ZkNy04ODEzLTQ0MDMtYjIwOS00ZDAxODU3YWZmNTQiLCJpc3MiOiJodHRwczovL2FwaS5wcmVwcm9kLmRldi5pbmZvbWFuaWFrLmNoLzEvYXR0ZXN0Iiwic3ViIjoiY29tLmluZm9tYW5pYWsuc3dpc3N0cmFuc2ZlciIsImF1ZCI6Imh0dHBzOi8vc3dpc3N0cmFuc2Zlci5wcmVwcm9kLmRldi5pbmZvbWFuaWFrLmNoL2FwaS9tb2JpbGUvY29udGFpbmVycyIsImV4cCI6MTc0MTg3NjEwNjAsImlhdCI6MTc0MTc4OTcwNn0.gNF8xcnXgm_ozjQoRQxmWzA6n7N3v-vsMp6QnFVPSwj4_VyHwwH1UeYRAzzvh-HZWe-DSQxgUr6plYzcrk4Xi8DVjEtigYHjo8M048Mf9qIxn7GOKnBscAg7-J6H-ss1Nc57DzOF_6mJdB302SfoWerKmB3Jorw-T6iT7zeEpPtiEMUHaJHxy-tiqMQp3bBHRp5yZRueT_dyy_by8s9n3n_6hTnw5lEjHRkwxX9Uyb7DJKnRcrytr6JO6tVMfDlrbJjHwcwOtafYWwJL6l_SrXUIy_1GK7Hb4kYMgGiAkaMGOxB2-2oZZYmCT1MSOrd88lDNgEDb_BOucEGWubkAH2yIgui--DjrTJBj4NhBZUhj4Vw7zCwOQvZkAa_4lXWBWwAeGKqicWk5UOgCXkcaN7TFj3v-FYXa8y_gRcoPb5dxykIF0lrsBmeYPtRHftUNEWWVx0aDfw91DJ5MLEnrRzyMGEoNXkJidWr6CgXwO2MdVuEsJtQ1_jOixo80i7tDRZArtkDGYrE3ZnP7lwCCzk91jiFaaYjPH-DNBnukMqEgppwDVuv0c4tQYkT4D4ktMoN15LQg3zZgH6uR4P1kCm1HNUvc_pY1f8ZzRq5JQCJgIwT3YbpndxDQh5i4v7aRK0XzHdk9L_1i6GgIYWebIODPmWJ-DydrwiSmMfo5MzQ"
    }
}
