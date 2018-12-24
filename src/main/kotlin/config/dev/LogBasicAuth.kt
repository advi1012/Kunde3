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
package de.hska.kunde.config.dev

import de.hska.kunde.config.logger
import java.util.Base64.getEncoder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description

/**
 * Interface, um einen CommandLineRunner zur Ausgabe für
 * _BASIC_-Authentifizierung im Profil _dev_ bereitstellen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface LogBasicAuth {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev"
     * bereitzustellen, der verschiedene Benutzerkennungen für
     * _BASIC_-Authentifizierung codiert.
     * @return CommandLineRunner
     */
    @Bean
    @Qualifier("LogBasicAuthRunner")
    @Description("Ausgabe fuer BASIC-Authentifizierung")
    fun logBasicAuth(
            // SonarQube "Credentials should not be hard-coded"
        @Value("\${kunde.password}") password: String,
        @Value("\${kunde.passwordFalsch}") passwordFalsch: String
    ) = CommandLineRunner {
        val usernameAdmin = "admin"
        val usernameAlpha1 = "alpha1"
        val charset = charset("ISO-8859-1")
        val logger = logger()

        var input = "$usernameAdmin:$password".toByteArray(charset)
        var encoded = "Basic ${getEncoder().encodeToString(input)}"
        logger.warn("BASIC Authentication   $usernameAdmin:$password   $encoded")
        input = "$usernameAdmin:$passwordFalsch".toByteArray(charset)
        encoded = "Basic ${getEncoder().encodeToString(input)}"
        logger.warn("BASIC Authentication   $usernameAdmin:$passwordFalsch   $encoded")
        input = "$usernameAlpha1:$password".toByteArray(charset)
        encoded = "Basic ${getEncoder().encodeToString(input)}"
        logger.warn("BASIC Authentication   $usernameAlpha1:$password   $encoded")
    }
}
