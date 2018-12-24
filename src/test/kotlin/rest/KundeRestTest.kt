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
import de.hska.kunde.config.security.CustomUserDetails
import de.hska.kunde.entity.Adresse
import de.hska.kunde.entity.GeschlechtType.WEIBLICH
import de.hska.kunde.entity.InteresseType.LESEN
import de.hska.kunde.entity.InteresseType.REISEN
import de.hska.kunde.entity.InteresseType.SPORT
import de.hska.kunde.entity.Kunde
import de.hska.kunde.entity.Kunde.Companion.ID_PATTERN
import de.hska.kunde.entity.Umsatz
import de.hska.kunde.rest.util.KundeConstraintViolation
import de.hska.kunde.rest.util.PatchOperation

import java.math.BigDecimal.ONE
import java.net.URL
import java.time.LocalDate
import java.util.Currency

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.get
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
        .RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpHeaders.IF_MATCH
import org.springframework.http.HttpHeaders.IF_NONE_MATCH
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.HttpStatus.PRECONDITION_FAILED
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.toMono

@Tag("rest")
@ExtendWith(SpringExtension::class)
// Alternative zu @ContextConfiguration von Spring
// Default: webEnvironment = MOCK, d.h.
//          Mock Servlet Umgebung anstatt eines Embedded Servlet Containers
@SpringBootTest(webEnvironment = RANDOM_PORT)
// @SpringBootTest(webEnvironment = DEFINED_PORT, ...)
// ggf.: @DirtiesContext, falls z.B. ein Spring Bean modifiziert wurde
@ActiveProfiles(DEV)
@TestPropertySource(locations = ["/rest-test.properties"])
@DisplayName("REST-Schnittstelle fuer Kunden testen")
@Suppress("ClassName")
class KundeRestTest(@LocalServerPort private val port: Int) {
    // WebClient auf der Basis von "Reactor Netty"
    private lateinit var client: WebClient
    private lateinit var baseUrl: String

    @BeforeAll
    @Suppress("unused")
    fun beforeAll() {
        baseUrl = "$SCHEMA://$HOST:$port"
        logger.info("baseUri = {}", baseUrl)
        // Alternative: Http Client von Java http://openjdk.java.net/groups/net/httpclient/intro.html
        client = WebClient.builder()
                .filter(basicAuthentication(USERNAME, PASSWORD))
                .baseUrl(baseUrl)
                .build()
    }

    @Test
    fun `Immer erfolgreich`() = assertTrue(true)

    @Test
    @Disabled("Noch nicht fertig")
    fun `Noch nicht fertig`() = assertFalse(true)

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Lesen {
        @Nested
        inner class `Suche anhand der ID` {
            @ParameterizedTest
            @ValueSource(strings = [ID_VORHANDEN, ID_UPDATE_PUT, ID_UPDATE_PATCH])
            fun `Suche mit vorhandener ID`(id: String) {
                // act
                val kunde = client.get()
                        .uri(ID_PATH, id)
                        .retrieve()
                        .bodyToMono<Kunde>()
                        .block()!!

                // assert
                logger.debug("Gefundener Kunde = {}", kunde)
                with(kunde) {
                    assertAll(
                            { assertTrue(nachname.isNotEmpty()) },
                            { assertTrue(email.isNotEmpty()) },
                            {
                                assertTrue(
                                        _links!!["self"]!!["href"]
                                        !!.endsWith("/$id")
                                )
                            }
                    )
                }
            }

            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, 0")
            fun `Suche mit vorhandener ID und vorhandener Version`(
                id: String,
                version: String
            ) {
                // act
                val response = client.get()
                        .uri(ID_PATH, id)
                        .header(IF_NONE_MATCH, version)
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NOT_MODIFIED, response.statusCode())
            }

            @ParameterizedTest
            @CsvSource("$ID_VORHANDEN, xxx")
            fun `Suche mit vorhandener ID und falscher Version`(
                id: String,
                version: String
            ) {
                // act
                val kunde = client.get()
                        .uri(ID_PATH, id)
                        .header(IF_NONE_MATCH, version)
                        .retrieve()
                        .bodyToMono<Kunde>()
                        .block()!!

                // assert
                logger.debug("Gefundener Kunde = {}", kunde)
                with(kunde) {
                    assertAll(
                            { assertTrue(nachname.isNotEmpty()) },
                            { assertTrue(email.isNotEmpty()) },
                            {
                                assertTrue(
                                        _links!!["self"]!!["href"]
                                        !!.endsWith("/$id")
                                )
                            }
                    )
                }
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_INVALID, ID_NICHT_VORHANDEN])
            fun `Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID`(id: String) {
                // act
                val response = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NOT_FOUND, response.statusCode())
            }

            @ParameterizedTest
            @CsvSource("$USERNAME, $PASSWORD_FALSCH, $ID_VORHANDEN")
            fun `Suche mit ID, aber falschem Passwort`(
                username: String,
                password: String,
                id: String
            ) {
                // arrange
                val clientFalsch = WebClient.builder()
                        .filter(basicAuthentication(username, password))
                        .baseUrl(baseUrl)
                        .build()

                // act
                val response = clientFalsch.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()!!

                // assert
                assertEquals(UNAUTHORIZED, response.statusCode())
            }
        }

        @Test
        fun `Suche nach allen Kunden`() {
            // act
            val kunden = client.get()
                    .retrieve()
                    .bodyToFlux<Kunde>()
                    .collectList()
                    .block()!!

            // assert
            assertTrue(kunden.isNotEmpty())
        }

        @ParameterizedTest
        @ValueSource(strings = [NACHNAME])
        fun `Suche mit vorhandenem Nachnamen`(nachname: String) {
            // arrange
            val nachnameLower = nachname.toLowerCase()

            // act
            val kunden = client.get()
                    .uri {
                        it.path(KUNDE_PATH)
                                .queryParam(NACHNAME_PARAM, nachnameLower)
                                .build()
                    }
                    .retrieve()
                    .bodyToFlux<Kunde>()
                    .collectList()
                    .block()!!

            // assert
            assertAll(
                    { assertTrue(kunden.isNotEmpty()) },
                    {
                        kunden.forEach {
                            assertEquals(nachnameLower, it.nachname.toLowerCase())
                        }
                    }
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [EMAIL_VORHANDEN])
        fun `Suche mit vorhandener Email`(email: String) {
            // act
            val kunden = client.get()
                    .uri {
                        it.path(KUNDE_PATH)
                                .queryParam(EMAIL_PARAM, email)
                                .build()
                    }
                    .retrieve()
                    .bodyToFlux<Kunde>()
                    .collectList()
                    .block()!!

            // assert
            assertAll(
                    { assertTrue(kunden.isNotEmpty()) },
                    {
                        val emails = kunden.map { it.email }
                        assertEquals(1, emails.size)
                        assertEquals(
                                email.toLowerCase(),
                                emails[0].toLowerCase()
                        )
                    }
            )
        }
    }

    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Schreiben {
        @Nested
        inner class Erzeugen {
            @ParameterizedTest
            @CsvSource(
                    "$NEUER_NACHNAME, $NEUE_EMAIL, $NEUES_GEBURTSDATUM, $CURRENCY_CODE, $NEUE_HOMEPAGE, $NEUE_PLZ, " +
                            "$NEUER_ORT, $NEUER_USERNAME"
            )
            fun `Abspeichern eines neuen Kunden`(args: ArgumentsAccessor) {
                // arrange
                val neuerKunde = Kunde(
                    id = null,
                    nachname = args.get<String>(0),
                    email = args.get<String>(1),
                    newsletter = true,
                    geburtsdatum = args.get<LocalDate>(2),
                    umsatz = Umsatz(betrag = ONE, waehrung = Currency.getInstance(args.get<String>(3))),
                    homepage = args.get<URL>(4),
                    geschlecht = WEIBLICH,
                    interessen = listOf(LESEN, REISEN),
                    adresse = Adresse(plz = args.get<String>(5), ort = args.get<String>(6))
                )
                neuerKunde.user = CustomUserDetails(
                    id = null,
                    username = args.get<String>(7),
                    password = "p",
                    authorities = emptyList()
                )

                // act
                val response = client.post()
                        .body(neuerKunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(CREATED, statusCode()) },
                            {
                                assertNotNull(headers())
                                val location = headers().asHttpHeaders().location
                                assertNotNull(location)
                                val locationStr = location.toString()
                                assertNotEquals("", locationStr)
                                val indexLastSlash = locationStr.lastIndexOf('/')
                                assertTrue(indexLastSlash > 0)
                                val idStr = locationStr.substring(indexLastSlash + 1)
                                assertTrue(idStr.matches(Regex(ID_PATTERN)))
                            },
                            { assertFalse(bodyToMono<String>().hasElement().block()!!) }
                    )
                }
            }

            @ParameterizedTest
            @CsvSource(
                    "$NEUER_NACHNAME_INVALID, $NEUE_EMAIL_INVALID, $NEUES_GEBURTSDATUM, $NEUE_PLZ_INVALID, $NEUER_ORT"
            )
            fun `Abspeichern eines neuen Kunden mit ungueltigen Werten`(args: ArgumentsAccessor) {
                // arrange
                val neuerKunde = Kunde(
                        id = null,
                        nachname = args.get<String>(0),
                        email = args.get<String>(1),
                        newsletter = true,
                        geburtsdatum = args.get<LocalDate>(2),
                        geschlecht = WEIBLICH,
                        interessen = listOf(LESEN, REISEN),
                        adresse = Adresse(plz = args.get<String>(3), ort = args.get<String>(4))
                )

                // act
                val response = client.post()
                        .body(neuerKunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(BAD_REQUEST, statusCode()) },
                            {
                                val violations = bodyToFlux<KundeConstraintViolation>().collectList().block()!!
                                assertTrue(violations.size == 3)
                                violations.map { it.message!! }
                                        .forEach {
                                            assertTrue(it.contains("ist nicht 5-stellig") ||
                                                    it.contains("Bei Nachnamen ist nach einem") ||
                                                    it.contains("Die EMail-Adresse"))
                                        }
                            }
                    )
                }
            }

            @ParameterizedTest
            @CsvSource(
                    "$NEUER_NACHNAME, $NEUE_EMAIL, $NEUES_GEBURTSDATUM, $CURRENCY_CODE, $NEUE_HOMEPAGE, $NEUE_PLZ, " +
                            "$NEUER_ORT, $NEUER_USERNAME"
            )
            fun `Abspeichern eines neuen Kunden mit vorhandenem Usernamen`(args: ArgumentsAccessor) {
                // arrange
                val neuerKunde = Kunde(
                        id = null,
                        nachname = args.get<String>(0),
                        email = "${args.get<String>(1)}x",
                        newsletter = true,
                        geburtsdatum = args.get<LocalDate>(2),
                        umsatz = Umsatz(betrag = ONE, waehrung = Currency.getInstance(args.get<String>(3))),
                        homepage = args.get<URL>(4),
                        geschlecht = WEIBLICH,
                        interessen = listOf(LESEN, REISEN),
                        adresse = Adresse(plz = args.get<String>(5), ort = args.get<String>(6))
                )
                neuerKunde.user = CustomUserDetails(
                        id = null,
                        username = args.get<String>(7),
                        password = "p",
                        authorities = emptyList()
                )

                // act
                val response = client.post()
                        .body(neuerKunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(BAD_REQUEST, statusCode()) },
                            {
                                val body = bodyToMono<String>().block()!!
                                assertTrue(body.contains("Username"))
                            }
                    )
                }
            }
        }

        @Nested
        inner class Aendern {
            @ParameterizedTest
            @ValueSource(strings = [ID_UPDATE_PUT])
            fun `Aendern eines vorhandenen Kunden durch Put`(id: String) {
                // arrange
                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val kundeOrig = responseOrig!!
                        .bodyToMono<Kunde>()
                        .block()!!
                assertNotNull(kundeOrig)
                val kunde = kundeOrig.copy(id = id, email = "${kundeOrig.email}put")

                val etag = responseOrig.headers().asHttpHeaders().eTag
                assertNotNull(etag)
                val version = etag!!.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.put()
                        .uri(ID_PATH, id)
                        .header(IF_MATCH, versionInt.toString())
                        .body(kunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(NO_CONTENT, statusCode()) },
                            { assertFalse(bodyToMono<String>().hasElement().block()!!) }
                    )
                }
                // ggf. noch GET-Request, um die Aenderung zu pruefen
            }

            @ParameterizedTest
            @CsvSource(
                    value = [
                        "$ID_UPDATE_PUT, $EMAIL_VORHANDEN",
                        "$ID_UPDATE_PATCH, $EMAIL_VORHANDEN"
                    ]
            )
            fun `Aendern eines Kunden durch Put und Email existiert`(
                id: String,
                email: String
            ) {
                // arrange
                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val kundeOrig = responseOrig!!
                        .bodyToMono<Kunde>()
                        .block()!!
                assertNotNull(kundeOrig)
                val kunde = kundeOrig.copy(id = id, email = email)

                val etag = responseOrig.headers().asHttpHeaders().eTag
                assertNotNull(etag)
                val version = etag!!.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.put()
                        .uri(ID_PATH, id)
                        .header(IF_MATCH, versionInt.toString())
                        .body(kunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(BAD_REQUEST, statusCode()) },
                            {
                                val body = bodyToMono<String>().block()!!
                                assertTrue(body.contains(email))
                            }
                    )
                }
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_VORHANDEN, ID_UPDATE_PUT, ID_UPDATE_PATCH])
            fun `Aendern eines Kunden durch Put ohne Version`(id: String) {
                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val kunde = responseOrig!!
                        .bodyToMono<Kunde>()
                        .block()!!
                assertNotNull(kunde)

                // act
                val response = client.put()
                        .uri(ID_PATH, id)
                        .body(kunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(PRECONDITION_FAILED, statusCode()) },
                            {
                                val body = bodyToMono<String>().block()!!
                                assertTrue(body.contains("Versionsnummer"))
                            }
                    )
                }
            }

            @ParameterizedTest
            @CsvSource(
                    value = [
                        "$ID_UPDATE_PUT, $NEUER_NACHNAME_INVALID, $NEUE_EMAIL_INVALID"
                    ]
            )
            fun `Aendern eines Kunden durch Put mit ungueltigen Daten`(
                id: String,
                nachname: String,
                email: String
            ) {
                // arrange
                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val kundeOrig = responseOrig!!
                        .bodyToMono<Kunde>()
                        .block()!!
                assertNotNull(kundeOrig)
                val kunde = kundeOrig.copy(id = id, nachname = nachname, email = email)

                val etag = responseOrig.headers().asHttpHeaders().eTag
                assertNotNull(etag)
                val version = etag!!.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.put()
                        .uri(ID_PATH, id)
                        .header(IF_MATCH, versionInt.toString())
                        .body(kunde.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(BAD_REQUEST, statusCode()) },
                            {
                                val violations = bodyToFlux<KundeConstraintViolation>().collectList().block()!!
                                assertTrue(violations.size == 2)
                                violations.map { it.message!! }
                                        .forEach {
                                            assertTrue(it.contains("Nachname") ||
                                                    it.contains("EMail-Adresse"))
                                        }
                            }
                    )
                }
            }

            @ParameterizedTest
            @CsvSource(value = ["$ID_UPDATE_PATCH, $NEUE_EMAIL"])
            fun `Aendern eines vorhandenen Kunden durch Patch`(
                id: String,
                email: String
            ) {
                // arrange
                val replaceOp = PatchOperation(
                        op = "replace",
                        path = "/email",
                        value = "${email}patch"
                )
                val addOp = PatchOperation(
                        op = "add",
                        path = "/interessen",
                        value = NEUES_INTERESSE.value
                )
                val removeOp = PatchOperation(
                        op = "remove",
                        path = "/interessen",
                        value = ZU_LOESCHENDES_INTERESSE.value
                )
                val operations = listOf(replaceOp, addOp, removeOp)

                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val etag = responseOrig!!.headers().asHttpHeaders().eTag
                assertNotNull(etag)
                val version = etag!!.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.patch()
                        .uri(ID_PATH, id)
                        .header(IF_MATCH, versionInt.toString())
                        .body(operations.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(NO_CONTENT, statusCode()) },
                            {
                                assertFalse(bodyToMono<String>().hasElement().block()!!)
                            }
                    )
                }
                // ggf. noch GET-Request, um die Aenderung zu pruefen
            }

            @ParameterizedTest
            @CsvSource(value = ["$ID_UPDATE_PATCH, $NEUE_EMAIL_INVALID"])
            fun `Aendern eines Kunden durch Patch mit ungueltigen Daten`(
                id: String,
                email: String
            ) {
                // arrange
                val replaceOp = PatchOperation(
                        op = "replace",
                        path = "/email",
                        value = email
                )
                val operations = listOf(replaceOp)

                val responseOrig = client.get()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()
                val etag = responseOrig!!.headers().asHttpHeaders().eTag
                assertNotNull(etag)
                val version = etag!!.substring(1, etag.length - 1)
                val versionInt = version.toInt() + 1

                // act
                val response = client.patch()
                        .uri(ID_PATH, id)
                        .header(IF_MATCH, versionInt.toString())
                        .body(operations.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(BAD_REQUEST, statusCode()) },
                            {
                                val violations =
                                    bodyToFlux<KundeConstraintViolation>().collectList().block()!!
                                assertTrue(violations.size == 1)

                                violations.map { it.message!! }
                                        .forEach {
                                            assertTrue(it.contains("EMail-Adresse"))
                                        }
                            }
                    )
                }
                // ggf. noch GET-Request, um die Aenderung zu pruefen
            }

            @ParameterizedTest
            @CsvSource(
                    value = [
                        "$ID_VORHANDEN, $NEUE_EMAIL_INVALID",
                        "$ID_UPDATE_PUT, $NEUE_EMAIL_INVALID",
                        "$ID_UPDATE_PATCH, $NEUE_EMAIL_INVALID"
                    ]
            )
            fun `Aendern eines Kunden durch Patch ohne Versionsnr`(
                id: String,
                email: String
            ) {
                // arrange
                val replaceOp = PatchOperation(
                        op = "replace",
                        path = "/email",
                        value = "${email}patch"
                )
                val operations = listOf(replaceOp)

                // act
                val response = client.patch()
                        .uri(ID_PATH, id)
                        .body(operations.toMono())
                        .exchange()
                        .block()!!

                // assert
                with(response) {
                    assertAll(
                            { assertEquals(PRECONDITION_FAILED, statusCode()) },
                            {
                                val body = bodyToMono<String>().block()!!
                                assertTrue(body.contains("Versionsnummer"))
                            }
                    )
                }
            }
        }

        @Nested
        inner class Loeschen {
            @ParameterizedTest
            @ValueSource(strings = [ID_DELETE])
            fun `Loeschen eines vorhandenen Kunden mit der ID`(id: String) {
                // act
                val response = client.delete()
                        .uri(ID_PATH, id)
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NO_CONTENT, response.statusCode())
            }

            @ParameterizedTest
            @ValueSource(strings = [EMAIL_DELETE])
            fun `Loeschen eines vorhandenen Kunden mit Emailadresse`(email: String) {
                // act
                val response = client.delete()
                        .uri {
                            it.path(KUNDE_PATH)
                                    .queryParam(EMAIL_PARAM, email)
                                    .build()
                        }
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NO_CONTENT, response.statusCode())
            }

            @ParameterizedTest
            @ValueSource(strings = [EMAIL_DELETE])
            fun `Loeschen mit nicht-vorhandener Emailadresse`(email: String) {
                // act
                val response = client.delete()
                        .uri {
                            it.path(KUNDE_PATH)
                                    .queryParam(EMAIL_PARAM, "${email}xxxx")
                                    .build()
                        }
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NO_CONTENT, response.statusCode())
            }

            @Test
            fun `Loeschen ohne Emailadresse`() {
                // act
                val response = client.delete()
                        .uri {
                            it.path(KUNDE_PATH)
                                    .queryParam(EMAIL_PARAM, null)
                                    .build()
                        }
                        .exchange()
                        .block()!!

                // assert
                assertEquals(NO_CONTENT, response.statusCode())
            }
        }
    }

    @Suppress("MayBeConstant")
    private companion object {
        val SCHEMA = "http"
        val HOST = "localhost"
        val KUNDE_PATH = "/"
        val ID_PATH = "/{id}"
        val NACHNAME_PARAM = "nachname"
        val EMAIL_PARAM = "email"

        const val USERNAME = "admin"
        const val PASSWORD = "p"
        const val PASSWORD_FALSCH = "Falsches Passwort!"

        const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
        const val ID_INVALID = "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val ID_UPDATE_PUT = "00000000-0000-0000-0000-000000000002"
        const val ID_UPDATE_PATCH = "00000000-0000-0000-0000-000000000003"
        const val ID_DELETE = "00000000-0000-0000-0000-000000000004"
        const val EMAIL_VORHANDEN = "alpha@hska.edu"
        const val EMAIL_DELETE = "phi@hska.cn"

        const val NACHNAME = "alpha"

        const val NEUE_PLZ = "12345"
        const val NEUE_PLZ_INVALID = "1234"
        const val NEUER_ORT = "Testort"
        const val NEUER_NACHNAME = "Neuernachname"
        const val NEUER_NACHNAME_INVALID = "?!&NachnameUngueltig"
        const val NEUE_EMAIL = "email@test.de"
        const val NEUE_EMAIL_INVALID = "emailungueltig@"
        const val NEUES_GEBURTSDATUM = "2017-01-31"
        const val CURRENCY_CODE = "EUR"
        const val NEUE_HOMEPAGE = "https://test.de"
        const val NEUER_USERNAME = "test"

        val NEUES_INTERESSE = SPORT
        val ZU_LOESCHENDES_INTERESSE = LESEN

        val logger = logger()
    }
}
