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

import de.hska.kunde.Router.Companion.idPathVar
import de.hska.kunde.Router.Companion.prefixPathVar
import de.hska.kunde.service.KundeValuesService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * Handler für die Abfrage von Werten (für "Software Engineering").
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class KundeValuesHandler(private val service: KundeValuesService) {
    /**
     * Abfrage, wie viele Kunden es gibt.
     * @param request Der eingehende Request
     * @return Die Anzahl der Kunden oder Statuscode 404, falls es keine gibt.
     */
    fun anzahlKunden(request: ServerRequest): Mono<ServerResponse> =
        service.anzahlKunden().flatMap { ok().body(it.toMono()) }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     * @param request Der eingehende Request mit dem Präfix als Pfadvariable.
     * @return Die passenden Nachnamen oder Statuscode 404, falls es keine gibt.
     */
    fun findNachnamenByPrefix(request: ServerRequest): Mono<ServerResponse> {
        val prefix = request.pathVariable(prefixPathVar)
        return service.findNachnamenByPrefix(prefix)
                .collectList()
                .flatMap {
                    if (it.isEmpty())
                        notFound().build()
                    else ok().body(it.toMono())
                }
    }

    /**
     * Abfrage, welche Emailadressen es zu einem Präfix gibt.
     * @param request Der eingehende Request mit dem Präfix als Pfadvariable.
     * @return Die passenden Emailadressen oder Statuscode 404, falls es keine gibt.
     */
    fun findEmailsByPrefix(request: ServerRequest): Mono<ServerResponse> {
        val prefix = request.pathVariable(prefixPathVar)
        return service.findEmailsByPrefix(prefix)
                .collectList()
                .flatMap {
                    if (it.isEmpty())
                        notFound().build()
                    else ok().body(it.toMono())
                }
    }

    /**
     * Abfrage, welche Version es zu einer Kunde-ID gibt.
     * @param request Der eingehende Request mit der Kunde-ID als Pfadvariable.
     * @return Die Versionsnummer.
     */
    fun findVersionById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)
        return service.findVersionById(id)
                .map { it.toString() }
                // version als String: kein Deserialisieren wie bei Entity-Kl.
                .flatMap { ok().body(it.toMono()) }
    }
}
