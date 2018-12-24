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
package de.hska.kunde.config.dev

import de.hska.kunde.config.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder

/**
 * Interface, um einen CommandLineRunner zur Ausgabe für
 * Verschlüsselungsverfahren bereitzustellen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface LogPasswordEncoding {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev"
     * bereitzustellen, der die Resultate verschiedener
     * Verschlüsselungsverfahren ausgibt
     * @return CommandLineRunner für die Ausgabe
     */
    @Bean
    @Description("Ausgabe fuer BCrypt")
    fun logBCrypt(
        passwordEncoder: PasswordEncoder,
        @Value("\${kunde.password}") password: String
    ): CommandLineRunner {
        val logger = logger()

        return CommandLineRunner {
            var verschluesselt = passwordEncoder.encode(password)
            logger.warn("bcrypt mit $password:   $verschluesselt")

            val pbkdf2PasswordEncoder = Pbkdf2PasswordEncoder()
            verschluesselt = pbkdf2PasswordEncoder.encode(password)
            logger.warn("PBKDF2 mit $password:   $verschluesselt")

            val scryptPasswordEncoder = SCryptPasswordEncoder()
            verschluesselt = scryptPasswordEncoder.encode(password)
            logger.warn("scrypt mit $password:   $verschluesselt")
        }
    }
}
