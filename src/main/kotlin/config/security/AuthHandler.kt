/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.kunde.config.security

import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.toMono

/**
 * Request-Handler, um die Rollen zur eigenen Benutzerkennung zu ermitteln.
 * Diese Funktionalität ist für "Software Engineering" im 4. Semester.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class AuthHandler(private val repo: CustomUserDetailsRepository) {
    /**
     * Die Rollen zur eigenen Benutzerkennung ermitteln.
     * @param request Das eingegangene Request-Objekt mit der Benutzerkennung
     *      als Objekt zum Interface Principal.
     * @return Response mit der Liste der eigenen Rollen oder Statuscode 401,
     *      falls man nicht eingeloggt ist.
     */
    fun findEigeneRollen(request: ServerRequest) =
        request.principal()
                .map { it.name }
                .flatMap { username ->
                    repo.findByUsername(username)
                            .flatMap { userDetails ->
                                val rollen = userDetails.authorities.map { it.authority }
                                ok().body(rollen.toMono())
                            }
                            .switchIfEmpty(status(UNAUTHORIZED).build())
                }
}
