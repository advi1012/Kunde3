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
package de.hska.kunde.config

import de.hska.kunde.config.security.CustomUserDetails
import de.hska.kunde.entity.FamilienstandType
import de.hska.kunde.entity.GeschlechtType
import de.hska.kunde.entity.InteresseType
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.convert.MongoCustomConversions

/**
 * Spring-Konfiguration für den Zugriff auf _MongoDB_.
 *
 * @author Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface DbConfig {
    /**
     * Liste mit Konvertern für Lesen und Schreiben in _MongoDB_ ermitteln.
     * @return Liste mit Konvertern für Lesen und Schreiben in _MongoDB_.
     */
    @Bean
    fun customConversions() = MongoCustomConversions(listOf(
            // Enums
            GeschlechtType.ReadConverter(),
            GeschlechtType.WriteConverter(),
            FamilienstandType.ReadConverter(),
            FamilienstandType.WriteConverter(),
            InteresseType.ReadConverter(),
            InteresseType.WriteConverter(),

            // Rollen fuer Security
            CustomUserDetails.RoleReadConverter(),
            CustomUserDetails.RoleWriteConverter()))
}
