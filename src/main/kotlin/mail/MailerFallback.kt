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
package de.hska.kunde.mail

import de.hska.kunde.config.logger
import de.hska.kunde.entity.Kunde
import org.springframework.stereotype.Component

/**
 * Fallback-Implementierung zum Mailer für neue Kunden.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class MailerFallback {
    /**
     * Fallback-Function für [Mailer.sendFallback], die von _Netflix Hystrix_
     * im Fehlerfall aufgerufen wird.
     * @param neuerKunde Das Objekt des neuen Kunden.
     */
    fun send(neuerKunde: Kunde) = logger.error("Fehler beim Senden der Email fuer: {}", neuerKunde)

    private companion object {
        val logger = logger()
    }
}
