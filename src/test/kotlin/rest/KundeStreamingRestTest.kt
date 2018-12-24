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
@file:Suppress("PackageDirectoryMismatch")

package de.hska.kunde.rest // ktlint-disable package-name

import de.hska.kunde.config.Settings.DEV
import de.hska.kunde.config.logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
        .RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.TEXT_EVENT_STREAM
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Tag("streamingRest")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@TestPropertySource(locations = ["/rest-test.properties"])
@DisplayName("Streaming fuer Kunden testen")
class KundeStreamingRestTest(@LocalServerPort private val port: Int) {
    private lateinit var client: WebClient
    private lateinit var baseUrl: String

    @BeforeAll
    @Suppress("unused")
    fun beforeAll() {
        baseUrl = "$SCHEMA://$HOST:$port"
        logger.info("baseUri = {}", baseUrl)
        client = WebClient.builder()
                .filter(basicAuthentication(USERNAME, PASSWORD))
                .baseUrl(baseUrl)
                .build()
    }

    @Test
    fun `Streaming mit allen Kunden`() {
        // act
        val response = client.get()
                .header(ACCEPT, TEXT_EVENT_STREAM.toString())
                .exchange()
                .block()!!

        // assert
        with(response) {
            assertAll(
                    { assertEquals(OK, statusCode()) },
                    {
                        val body = bodyToMono<String>().block()
                        assertNotNull(body)
                        body as String
                        assertTrue(body.startsWith("data:"))
                    },
                    {
                        // TODO List<Kunde> durch ObjectMapper von Jackson
                        logger.warn("TODO: List<Kunde> ueberpruefen")
                    }
            )
        }
    }

    @Suppress("MayBeConstant")
    private companion object {
        val SCHEMA = "http"
        val HOST = "localhost"
        val USERNAME = "admin"
        val PASSWORD = "p"

        val logger = logger()
    }
}
