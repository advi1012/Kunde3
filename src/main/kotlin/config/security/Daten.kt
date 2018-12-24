
/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import org.springframework.security.core.authority.SimpleGrantedAuthority
import reactor.core.publisher.Flux

private val roleKunde = SimpleGrantedAuthority("ROLE_KUNDE")
private val roleAdmin = SimpleGrantedAuthority("ROLE_ADMIN")
private val roleActuator = SimpleGrantedAuthority("ROLE_ACTUATOR")
@Suppress("MayBeConstant")
private val password = "{bcrypt}\$2a\$10\$csH5eXtni40aoCepKV.JreDTxwJi0xYzH6rPZ8bdtyTeXLbQ8y2vq"

/**
 * Testdaten für Benutzernamen, PAsswörter und Rollen
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
val users = Flux.just(
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000000",
                username = "admin",
                password = password,
                authorities = listOf(
                        roleAdmin,
                        roleKunde,
                        roleActuator)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000001",
                username = "alpha1",
                password = password,
                authorities = listOf(roleKunde)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000002",
                username = "alpha2",
                password = password,
                authorities = listOf(roleKunde)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000003",
                username = "alpha3",
                password = password,
                authorities = listOf(roleKunde)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000004",
                username = "delta",
                password = password,
                authorities = listOf(roleKunde)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000005",
                username = "epsilon",
                password = password,
                authorities = listOf(roleKunde)),
        CustomUserDetails(
                id = "10000000-0000-0000-0000-000000000006",
                username = "phi",
                password = password,
                authorities = listOf(roleKunde))
)
