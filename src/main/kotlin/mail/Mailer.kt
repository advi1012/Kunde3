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

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import de.hska.kunde.config.MailAddressProps
import de.hska.kunde.config.logger
import de.hska.kunde.entity.Kunde
import org.springframework.integration.support.MessageBuilder.withPayload
import org.springframework.stereotype.Component

/**
 * Kafka-Client, der Nachrichten sendet, die als Email verschickt werden sollen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Component
class Mailer(
    private val mailOutput: MailOutput,
    private val props: MailAddressProps,
    private val fallback: MailerFallback
) {

    /**
     * Nachricht senden, dass es einen neuen Kunden gibt.
     * @param neuerKunde Das Objekt des neuen Kunden.
     */
    @HystrixCommand(fallbackMethod = "sendFallback")
    fun send(neuerKunde: Kunde) {
        val mail = Mail(
                to = props.sales,
                from = props.from,
                subject = "Neuer Kunde ${neuerKunde.id}",
                body = "<b>Neuer Kunde:</b> <i>${neuerKunde.nachname}</i>")
        logger.trace("$mail")
        mailOutput.getChannel().send(withPayload(mail).build())
    }

    /**
     * Fallback-Function für [Mailer.send], die von _Netflix Hystrix_ im
     * Fehlerfall aufgerufen wird.
     * @param neuerKunde Das Objekt des neuen Kunden.
     */
    @Suppress("unused")
    fun sendFallback(neuerKunde: Kunde) = fallback.send(neuerKunde)

    private companion object {
        val logger = logger()
    }
}
