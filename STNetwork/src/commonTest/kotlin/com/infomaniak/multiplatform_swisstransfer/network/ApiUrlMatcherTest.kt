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
package com.infomaniak.multiplatform_swisstransfer.network

import com.infomaniak.multiplatform_swisstransfer.network.utils.ApiUrlMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApiUrlMatcherTest {

    @Test
    fun `isV1Url valid URL with subdomain`() {
        assertTrue(ApiUrlMatcher.isV1Url("https://host.swisstransfer.com/d/uuid123"))
    }

    @Test
    fun `isV1Url valid URL without subdomain`() {
        assertTrue(ApiUrlMatcher.isV1Url("https://host/d/uuid123"))
    }

    @Test
    fun `isV1Url with query params`() {
        assertFalse(ApiUrlMatcher.isV1Url("https://host/d/uuid123?password=123"))
    }

    @Test
    fun `isV1Url v2 URL`() {
        assertFalse(ApiUrlMatcher.isV1Url("https://host/dl/uuid123"))
    }

    @Test
    fun `isV1Url without d path`() {
        assertFalse(ApiUrlMatcher.isV1Url("https://host/path/uuid123"))
    }

    @Test
    fun `isV1Url without https`() {
        assertFalse(ApiUrlMatcher.isV1Url("http://host/d/uuid123"))
    }

    @Test
    fun `isV1Url ending with slash`() {
        assertFalse(ApiUrlMatcher.isV1Url("https://host/d/"))
    }

    @Test
    fun `isV1Url empty string`() {
        assertFalse(ApiUrlMatcher.isV1Url(""))
    }

    @Test
    fun `isV2Url valid URL with subdomain`() {
        assertTrue(ApiUrlMatcher.isV2Url("https://host.swisstransfer.com/dl/uuid123"))
    }

    @Test
    fun `isV2Url valid URL without subdomain`() {
        assertTrue(ApiUrlMatcher.isV2Url("https://host/dl/uuid123"))
    }

    @Test
    fun `isV2Url with query params`() {
        assertFalse(ApiUrlMatcher.isV2Url("https://host/dl/uuid123?password=123"))
    }

    @Test
    fun `isV2Url v1 URL`() {
        assertFalse(ApiUrlMatcher.isV2Url("https://host/d/uuid123"))
    }

    @Test
    fun `isV2Url without dl path`() {
        assertFalse(ApiUrlMatcher.isV2Url("https://host/path/uuid123"))
    }

    @Test
    fun `isV2Url without https`() {
        assertFalse(ApiUrlMatcher.isV2Url("http://host/dl/uuid123"))
    }

    @Test
    fun `isV2Url ending with slash`() {
        assertFalse(ApiUrlMatcher.isV2Url("https://host/dl/"))
    }

    @Test
    fun `isV2Url empty string`() {
        assertFalse(ApiUrlMatcher.isV2Url(""))
    }

    @Test
    fun `extractUuid simple v1 URL`() {
        assertEquals("uuid123", ApiUrlMatcher.extractUUID("https://host/d/uuid123"))
    }

    @Test
    fun `extractUuid simple v2 URL`() {
        assertEquals("uuid123", ApiUrlMatcher.extractUUID("https://host/dl/uuid123"))
    }

    @Test
    fun `extractUuid URL ending with slash`() {
        assertEquals("", ApiUrlMatcher.extractUUID("https://host/d/"))
    }

    @Test
    fun `extractUuid without slash`() {
        assertEquals("uuid123", ApiUrlMatcher.extractUUID("uuid123"))
    }

    @Test
    fun `extractUuid nested path`() {
        assertEquals("uuid123", ApiUrlMatcher.extractUUID("https://host/d/path/uuid123"))
    }
}
