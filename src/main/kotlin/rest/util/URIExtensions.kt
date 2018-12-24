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

import de.hska.kunde.rest.util.HateoasConstants.HREF
import de.hska.kunde.rest.util.HateoasConstants.REL
import de.hska.kunde.rest.util.HateoasConstants.SELF
import java.net.URI

// Vereinfachte Variante fuer Spring HATEOAS (auf Basis von Spring MVC)
// https://github.com/spring-projects/spring-boot/blob/master/...
//       ...spring-boot-autoconfigure/src/main/java/org/springframework/boot...
//       .../autoconfigure/hateoas/HypermediaAutoConfiguration.java

/**
 * Extension-Function für URI, um zu einer URI, die HATEOAS-Links zu einem
 * Datensatz zu erstellen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
fun URI.singleLinks(): Map<String, Map<String, String>> {
    val list = "list"
    val add = "add"
    val update = "update"
    val remove = "remove"

    val selfUriStr: String = this.toString()
    val baseUri by lazy {
        val indexLastSlash = selfUriStr.lastIndexOf('/')
        selfUriStr.substring(0, indexLastSlash)
    }

    return mapOf(
            SELF to mapOf(HREF to selfUriStr),
            list to mapOf(HREF to baseUri),
            add to mapOf(HREF to baseUri),
            update to mapOf(HREF to selfUriStr),
            remove to mapOf(HREF to selfUriStr))
}

/**
 * Extension-Function für URI, um zu einer Basis-URI und einer UUID, die HATEOAS-Links
 * zu einem Datensatz innerhalb eines JSON-Arrays zu erstellen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
fun URI.itemLinks(id: String): List<Map<String, String>> {
    val scheme = this.scheme
    val host = this.host
    val port = this.port
    val path = this.path
    val uri = URI(scheme, null, host, port, path, null, null)
    return listOf(
            mapOf(REL to SELF),
            mapOf(HREF to "$uri$id"))
}
