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

package de.hska.kunde.service // ktlint-disable package-name

import de.hska.kunde.config.security.CustomUserDetails
import de.hska.kunde.config.security.CustomUserDetailsService
import de.hska.kunde.db.KundeRepository
import de.hska.kunde.entity.Kunde
import de.hska.kunde.entity.Umsatz
import de.hska.kunde.entity.Adresse
import de.hska.kunde.entity.FamilienstandType.LEDIG
import de.hska.kunde.entity.GeschlechtType.WEIBLICH
import de.hska.kunde.entity.InteresseType.LESEN
import de.hska.kunde.entity.InteresseType.REISEN
import de.hska.kunde.mail.Mailer
import java.math.BigDecimal.ONE
import java.net.URL
import java.time.LocalDate
import java.util.UUID.randomUUID
import java.util.Currency
import java.util.Locale.GERMANY
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.aggregator.ArgumentsAccessor
import org.junit.jupiter.params.aggregator.get
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.util.LinkedMultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.toMono
import reactor.core.publisher.Mono

@Tag("service")
@ExtendWith(MockitoExtension::class)
@DisplayName("Anwendungskern fuer Kunden testen")
class KundeServiceTest {
    private lateinit var service: KundeService

    @Mock
    private lateinit var repo: KundeRepository

    @Mock
    private lateinit var mongoTemplate: ReactiveMongoTemplate

    @Mock
    private lateinit var userDetailsService: CustomUserDetailsService

    @Mock
    private lateinit var mailer: Mailer

    @BeforeEach
    fun beforeEach() {
        initMocks(KundeService::class.java)
        assertNotNull(repo)
        assertNotNull(mongoTemplate)
        assertNotNull(userDetailsService)
        assertNotNull(mailer)

        service = KundeService(mongoTemplate, repo, userDetailsService, mailer)
    }

    @Test
    fun `Immer erfolgreich`() {
        assertTrue(true)
    }

    @Test
    @Disabled
    fun `Noch nicht fertig`() {
        assertTrue(false)
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Lesen {
        @Suppress("ClassName")
        @Nested
        inner class `Suche anhand der ID` {
            @ParameterizedTest
            @CsvSource(value = ["$ID_VORHANDEN, $NACHNAME"])
            fun `Suche mit vorhandener ID`(id: String, nachname: String) {
                // arrange
                val kundeMock = createKundeMock(id, nachname)
                given(repo.findById(id)).willReturn(kundeMock.toMono())

                // act
                val kunde = service.findById(id).block()!!

                // assert
                assertEquals(id, kunde.id)
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_NICHT_VORHANDEN])
            fun `Suche mit nicht vorhandener ID`(id: String) {
                // arrange
                given(repo.findById(id)).willReturn(Mono.empty())

                // act
                val result = service.findById(id).block()

                // assert
                assertNull(result)
            }
        }

        @ParameterizedTest
        @ValueSource(strings = [NACHNAME])
        fun `Suche alle Kunden`(nachname: String) {
            // arrange
            val kundeMock = createKundeMock(nachname)
            given(mongoTemplate.findAll(eq(Kunde::class.java))).willReturn(Flux.just(kundeMock))
            val emptyQueryParams = LinkedMultiValueMap<String, String>()

            // act
            val kunden = service.find(emptyQueryParams).collectList().block()!!

            // assert
            assertTrue(kunden.isNotEmpty())
        }

        @ParameterizedTest
        @ValueSource(strings = [NACHNAME])
        fun `Suche mit vorhandenem Nachnamen`(nachname: String) {
            // arrange
            val queryParams = LinkedMultiValueMap(mapOf("nachname" to listOf(nachname.toLowerCase())))
            val kundeMock = createKundeMock(nachname)
            given(mongoTemplate.find(any(Query::class.java), eq(Kunde::class.java)))
                    .willReturn(Flux.just(kundeMock))

            // act
            val kunden = service.find(queryParams).collectList().block()!!

            // assert
            assertAll(
                    { assertTrue(kunden.isNotEmpty()) },
                    { kunden.forEach { assertEquals(nachname, it.nachname) } }
            )
        }

        @ParameterizedTest
        @CsvSource(value = ["$ID_VORHANDEN, $NACHNAME, $EMAIL"])
        fun `Suche mit vorhandener Emailadresse`(id: String, nachname: String, email: String) {
            // arrange
            val queryParams = LinkedMultiValueMap(mapOf("email" to listOf(email)))
            val kundeMock = createKundeMock(id, nachname, email.toLowerCase())
            given(mongoTemplate.find(any(Query::class.java), eq(Kunde::class.java)))
                .willReturn(Flux.just<Kunde>(kundeMock))

            // act
            val kunden = service.find(queryParams).collectList().block()!!

            // assert
            assertAll(
                    { assertEquals(1, kunden.size) },
                    {
                        val kunde = kunden[0]
                        assertNotNull(kunde)
                        assertEquals(email.toLowerCase(), kunde.email)
                    }
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [EMAIL])
        fun `Suche mit nicht-vorhandener Emailadresse`(email: String) {
            // arrange
            val queryParams = LinkedMultiValueMap(mapOf("email" to listOf(email)))
            given(mongoTemplate.find(any(Query::class.java), eq(Kunde::class.java))).willReturn(Flux.empty<Kunde>())

            // act
            val result = service.find(queryParams).collectList().block()!!

            // assert
            assertTrue(result.isEmpty())
        }

        @ParameterizedTest
        @CsvSource(value = ["$ID_VORHANDEN, $NACHNAME, $EMAIL, $PLZ"])
        fun `Suche mit vorhandener PLZ`(id: String, nachname: String, email: String, plz: String) {
            // arrange
            val queryParams = LinkedMultiValueMap<String, String>(mapOf("plz" to listOf(plz)))
            val kundeMock = createKundeMock(id, nachname, email, plz)
            given(mongoTemplate.find(any(Query::class.java), eq(Kunde::class.java)))
                    .willReturn(Flux.just(kundeMock))

            // act
            val kunden = service.find(queryParams).collectList().block()!!

            // assert
            kunden.map { it.adresse.plz }
                    .forEach { assertEquals(plz, it) }
        }

        @ParameterizedTest
        @CsvSource(value = ["$ID_VORHANDEN, $NACHNAME, $EMAIL, $PLZ"])
        fun `Suche mit vorhandenem Nachnamen und PLZ`(id: String, nachname: String, email: String, plz: String) {
            // arrange
            val queryParams =
                LinkedMultiValueMap(mapOf("nachname" to listOf(nachname.toLowerCase()), "plz" to listOf(plz)))
            val kundeMock = createKundeMock(id, nachname, email, plz)
            given(mongoTemplate.find(any(Query::class.java), eq(Kunde::class.java)))
                    .willReturn(Flux.just(kundeMock))

            // act
            val kunden = service.find(queryParams).collectList().block()!!

            // assert
            kunden.forEach {
                assertAll(
                        { assertEquals(nachname.toLowerCase(), it.nachname.toLowerCase()) },
                        { assertEquals(plz, it.adresse.plz) }
                )
            }
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
            @CsvSource(value = ["$NACHNAME, $EMAIL, $PLZ, $USERNAME, $PASSWORD"])
            fun `Neuen Kunden abspeichern`(args: ArgumentsAccessor) {
                // arrange
                val nachname = args.get<String>(0)
                val email = args.get<String>(1)
                val plz = args.get<String>(2)
                val username = args.get<String>(3)
                val password = args.get<String>(4)

                given(repo.findByEmail(email)).willReturn(Mono.empty())
                val userDetailsMock = CustomUserDetails(id = null, username = username, password = password)
                val userDetailsMockCreated =
                    CustomUserDetails(id = randomUUID().toString(), username = username, password = password)
                given(userDetailsService.create(userDetailsMock)).willReturn(userDetailsMockCreated.toMono())
                val kundeMock = createKundeMock(null, nachname, email, plz, username, password)
                val kundeMockResult = kundeMock.copy(id = randomUUID().toString())
                given(mongoTemplate.save(kundeMock)).willReturn(kundeMockResult.toMono())

                // act
                val kunde = service.create(kundeMock).block()!!

                // assert
                assertAll(
                        { assertNotNull(kunde.id) },
                        { assertEquals(nachname, kunde.nachname) },
                        { assertEquals(email, kunde.email) },
                        { assertEquals(plz, kunde.adresse.plz) },
                        { assertEquals(username, kunde.username) }
                )
            }

            @ParameterizedTest
            @CsvSource(value = ["$NACHNAME, $EMAIL, $PLZ"])
            fun `Neuer Kunde ohne Benutzerdaten`(nachname: String, email: String, plz: String) {
                // arrange
                val kundeMock = createKundeMock(null, nachname, email, plz)

                // act
                val thrown: InvalidAccountException = assertThrows { service.create(kundeMock).block() }

                // assert
                assertNull(thrown.cause)
            }

            @ParameterizedTest
            @CsvSource(value = ["$NACHNAME, $EMAIL, $PLZ, $USERNAME, $PASSWORD"])
            fun `Neuer Kunde mit existierender Email`(args: ArgumentsAccessor) {
                // arrange
                val nachname = args.get<String>(0)
                val email = args.get<String>(1)
                val plz = args.get<String>(2)
                val username = args.get<String>(3)
                val password = args.get<String>(4)

                val userDetailsMock = CustomUserDetails(id = null, username = username, password = password)
                val userDetailsMockCreated =
                    CustomUserDetails(id = randomUUID().toString(), username = username, password = password)
                given(userDetailsService.create(userDetailsMock)).willReturn(userDetailsMockCreated.toMono())
                val kundeMock = createKundeMock(null, nachname, email, plz, username, password)
                given(repo.findByEmail(email)).willReturn(kundeMock.toMono())

                // act
                val thrown: EmailExistsException = assertThrows { service.create(kundeMock).block()!! }

                // assert
                assertAll(
                        { assertEquals(EmailExistsException::class, thrown::class) },
                        { assertNull(thrown.cause) }
                )
            }
        }

        @Nested
        inner class Aendern {
            @ParameterizedTest
            @CsvSource(value = ["$ID_UPDATE, $NACHNAME, $EMAIL, $PLZ"])
            fun `Vorhandenen Kunden aktualisieren`(id: String, nachname: String, email: String, plz: String) {
                // arrange
                val kundeMock = createKundeMock(id, nachname, email, plz)
                given(repo.findById(id)).willReturn(kundeMock.toMono())
                given(repo.findByEmail(email)).willReturn(Mono.empty())
                given(mongoTemplate.save(kundeMock)).willReturn(kundeMock.toMono())

                // act
                val kunde = service.update(kundeMock, id, kundeMock.version.toString()).block()!!

                // assert
                assertEquals(id, kunde.id)
            }

            @ParameterizedTest
            @CsvSource(value = ["$ID_NICHT_VORHANDEN, $NACHNAME, $EMAIL, $PLZ, $VERSION"])
            fun `Nicht-existierenden Kunden aktualisieren`(args: ArgumentsAccessor) {
                // arrange
                val id = args.get<String>(0)
                val nachname = args.get<String>(1)
                val email = args.get<String>(2)
                val plz = args.get<String>(3)
                val version = args.get<String>(4)

                val kundeMock = createKundeMock(id, nachname, email, plz)
                given(repo.findById(id)).willReturn(Mono.empty())

                // act
                val result = service.update(kundeMock, id, version).block()

                // assert
                assertNull(result)
            }

            @ParameterizedTest
            @CsvSource(value = ["$ID_UPDATE, $NACHNAME, $EMAIL, $PLZ, $VERSION_INVALID"])
            fun `Kunde aktualisieren mit falscher Versionsnummer`(args: ArgumentsAccessor) {
                // arrange
                val id = args.get<String>(0)
                val nachname = args.get<String>(1)
                val email = args.get<String>(2)
                val plz = args.get<String>(3)
                val version = args.get<String>(4)

                val kundeMock = createKundeMock(id, nachname, email, plz)
                given(repo.findById(id)).willReturn(kundeMock.toMono())

                // act
                val thrown: InvalidVersionException = assertThrows { service.update(kundeMock, id, version).block() }

                // assert
                assertEquals(thrown.message, "Falsche Versionsnummer $version")
            }

            @ParameterizedTest
            @CsvSource(value = ["$ID_UPDATE, $NACHNAME, $EMAIL, $PLZ, $VERSION_ALT"])
            fun `Kunde aktualisieren mit alter Versionsnummer`(args: ArgumentsAccessor) {
                // arrange
                val id = args.get<String>(0)
                val nachname = args.get<String>(1)
                val email = args.get<String>(2)
                val plz = args.get<String>(3)
                val version = args.get<String>(4)

                val kundeMock = createKundeMock(id, nachname, email, plz)
                given(repo.findById(id)).willReturn(kundeMock.toMono())

                // act
                val thrown: InvalidVersionException = assertThrows { service.update(kundeMock, id, version).block() }

                // assert
                assertNull(thrown.cause)
            }
        }

        @Nested
        inner class Loeschen {
            @ParameterizedTest
            @CsvSource(value = ["$ID_LOESCHEN, $NACHNAME"])
            fun `Vorhandenen Kunden loeschen`(id: String, nachname: String) {
                // arrange
                val kundeMock = createKundeMock(id, nachname)
                given(repo.findById(id)).willReturn(kundeMock.toMono())
                given(repo.deleteById(id)).willReturn(Mono.empty())

                // act
                val kunde = service.deleteById(id).block()!!

                // assert
                assertEquals(id, kunde.id)
            }

            @ParameterizedTest
            @ValueSource(strings = [ID_LOESCHEN_NICHT_VORHANDEN])
            fun `Nicht-vorhandenen Kunden loeschen`(id: String) {
                // arrange
                given(repo.findById(id)).willReturn(Mono.empty())

                // act
                val result = service.deleteById(id).block()

                // assert
                assertNull(result)
            }

            @ParameterizedTest
            @ValueSource(strings = [EMAIL])
            fun `Kunde mittels Email loeschen`(email: String) {
                // arrange
                given(repo.deleteByEmail(email)).willReturn(1L.toMono())

                // act
                val anzahl = service.deleteByEmail(email).block()!!

                // assert
                assertEquals(1, anzahl)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mocking
    // -------------------------------------------------------------------------
    private fun createKundeMock(nachname: String): Kunde = createKundeMock(randomUUID().toString(), nachname)

    private fun createKundeMock(id: String, nachname: String): Kunde = createKundeMock(id, nachname, EMAIL)

    private fun createKundeMock(id: String, nachname: String, email: String) = createKundeMock(id, nachname, email, PLZ)

    private fun createKundeMock(id: String?, nachname: String, email: String, plz: String) =
        createKundeMock(id, nachname, email, plz, null, null)

    @SuppressWarnings("LongParameterList")
    private fun createKundeMock(
        id: String?,
        nachname: String,
        email: String,
        plz: String,
        username: String?,
        password: String?
    ): Kunde {
        val adresse = Adresse(plz = plz, ort = ORT)
        val kunde = Kunde(
                id = id,
                version = 0,
                nachname = nachname,
                email = email,
                newsletter = true,
                umsatz = Umsatz(betrag = ONE, waehrung = WAEHRUNG),
                homepage = URL(HOMEPAGE),
                geburtsdatum = GEBURTSDATUM,
                geschlecht = WEIBLICH,
                familienstand = LEDIG,
                interessen = listOf(LESEN, REISEN),
                adresse = adresse,
                username = USERNAME
        )
        if (username != null && password != null) {
            val customUserDetails = CustomUserDetails(id = null, username = username, password = password)
            kunde.user = customUserDetails
        }
        return kunde
    }

    @Suppress("MayBeConstant")
    private companion object {
        const val ID_VORHANDEN = "00000000-0000-0000-0000-000000000001"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val ID_UPDATE = "00000000-0000-0000-0000-000000000002"
        const val ID_LOESCHEN = "00000000-0000-0000-0000-000000000005"
        const val ID_LOESCHEN_NICHT_VORHANDEN = "AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"
        const val PLZ = "12345"
        val ORT = "Testort"
        const val NACHNAME = "Test"
        const val EMAIL = "theo@test.de"
        val GEBURTSDATUM = LocalDate.of(2018, 1, 1)
        val WAEHRUNG = Currency.getInstance(GERMANY)
        val HOMEPAGE = "https://test.de"
        const val USERNAME = "test"
        const val PASSWORD = "p"
        const val VERSION = "0"
        const val VERSION_INVALID = "!?"
        const val VERSION_ALT = "-1"
    }
}
