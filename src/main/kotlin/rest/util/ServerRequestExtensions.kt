/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.kunde.rest.util

import org.springframework.http.HttpHeaders.IF_MATCH
import org.springframework.http.HttpHeaders.IF_NONE_MATCH
import org.springframework.http.codec.multipart.Part
import org.springframework.web.reactive.function.server.ServerRequest

/**
 * Extension Function für ServerRequest, um aus dem Header den ersten Wert zu
 * `If-Match` auszulesen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @return Erster Wert zu `If-Match` oder `null`.
 */
fun ServerRequest.ifMatch() = this.headers().header(IF_MATCH).firstOrNull()

/**
 * Extension Function für ServerRequest, um aus dem Header den ersten Wert zu
 * `If-None-Match` auszulesen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @return Erster Wert zu `If-None-Match` oder `null`.
 */
fun ServerRequest.ifNoneMatch() = this.headers().header(IF_NONE_MATCH).firstOrNull()

/**
 * Extension Function für ServerRequest, um aus dem Header den Wert zu
 * `Content-Type` auszulesen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @return Wert zu `Content-Type` oder `null`.
 */
fun ServerRequest.contentType(): String? {
    // Kein Optional, da Kotlin null-safe ist
    val contentTypeOpt = this.headers().contentType()
    return if (contentTypeOpt.isPresent)
        contentTypeOpt.get().toString()
    else null
}

/**
 * Extension Function für Part (bei einem Request mit MIME-Type
 * `multipart/form-data`), um den Wert zu `Content-Type` auszulesen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @return Wert zu `Content-Type` oder `null`.
 */
fun Part.contentType(): String? = this.headers().contentType?.toString()
