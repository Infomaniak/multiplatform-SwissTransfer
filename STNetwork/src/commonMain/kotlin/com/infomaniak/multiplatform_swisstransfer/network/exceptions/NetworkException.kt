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
package com.infomaniak.multiplatform_swisstransfer.network.exceptions

import io.ktor.utils.io.errors.IOException

/**
 * Thrown when a network-related error occurs, such as connectivity issues or timeouts.
 *
 * This exception is used to represent errors that occur during network operations,
 * typically indicating problems with the connection, DNS resolution, or other network failures.
 *
 * @param message A detailed message describing the network error.
 */
class NetworkException(message: String) : IOException(message)
