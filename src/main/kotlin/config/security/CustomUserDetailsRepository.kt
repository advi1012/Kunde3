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

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.security.core.userdetails.UserDetails
import reactor.core.publisher.Mono

/**
 * Repository gemäß Spring Data, um Benutzerkennungen aus MongoDB auszulesen.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface CustomUserDetailsRepository
    : ReactiveCrudRepository<CustomUserDetails, String> {
    /**
     * Suche nach einer Benutzerkennung anhand des Benutzernamens.
     * @param username Der Benutzer- bzw. Loginname
     * @return Die zugehörige Benutzerkennung oder ein leeres Mono-Objekt
     */
    fun findByUsername(username: String?): Mono<UserDetails>
}
