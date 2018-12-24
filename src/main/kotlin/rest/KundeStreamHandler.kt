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
package de.hska.kunde.rest

import de.hska.kunde.rest.util.itemLinks
import de.hska.kunde.service.KundeStreamService
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono

/**
 * Eine Handler-Function wird von der Router-Function [de.hska.kunde.Router.router]
 * aufgerufen, nimmt einen Request entgegen und erstellt den Response.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @constructor Einen KundeStreamHandler mit einem injizierten
 *      [de.hska.kunde.service.KundeStreamService] erzeugen.
 */
@Component
class KundeStreamHandler(private val service: KundeStreamService) {
    /**
     * Alle Kunden als Event-Stream zurückliefern.
     * @param request Das eingehende Request-Objekt.
     * @return Response mit dem MIME-Typ `text/event-stream`.
     */
    fun findAll(request: ServerRequest): Mono<ServerResponse> {
        val listUri = request.uri()
        val kunden = service.findAll().map {
            if (it.id != null) {
                it.links = listUri.itemLinks(it.id)
            }
            it
        }
        return ok().contentType(TEXT_EVENT_STREAM).body(kunden)
    }
}
