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
@file:Suppress("unused", "FunctionName")

package de.hska.kunde.db

import de.hska.kunde.entity.Kunde
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

// Queries werden durch Namenskonventionen deklariert wie bei Ruby-on-Rails.
// Von ReactiveCrudRepository sind u.a. folgende Interfaces abgeleitet:
// * ReactiveMongoRepository
// * ReactiveJpaRepository

/**
 * Interface für ein _Repository_ gemäß DDD für _MongoDB_.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface KundeRepository : ReactiveCrudRepository<Kunde, String> {
    /**
     * Suche nach einem Kunden mit der gegebenen Emailadresse.
     * @param email Die Emailadresse des gesuchten Kunden.
     * @return Der gefundene Kunde oder empty.
     */
    // SELECT * FROM kunde WHERE email = ...
    fun findByEmail(email: String): Mono<Kunde>

    /**
     * Suche nach Kunden mit dem gegebenen Nachnamen ohne Unterscheidung
     * zwischen Gross- und Kleinschreibung.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden.
     * @return Die gefundenen Kunden als Flux.
     */
    // SELECT * FROM kunde WHERE nachname LIKE ...
    fun findByNachnameIgnoreCase(nachname: String): Flux<Kunde>

    /**
     * Suche nach Kunden mit einem Nachnamen, der den gegebenen Teilstring
     * ohne Unterscheidung zwischen Gross- und Kleinschreibung enthält.
     * @param nachname Der gemeinsame Teilstring im Nachname der gesuchten
     *      Kunden.
     * @return Die gefundenen Kunden als Flux.
     */
    // SELECT * FROM kunde WHERE nachname ... LIKE ...
    fun findByNachnameContainingIgnoreCase(nachname: String): Flux<Kunde>

    /**
     * Suche nach Kunden mit dem gegebenen Nachnamen und sortiert nach der
     * Emailadresse.
     * @param nachname Der gemeinsame Nachname der gesuchten Kunden.
     * @return Die gefundenen Kunden als Flux mit Sortierung gemäß
     *      ihrer Emailadresse.
     */
    // SELECT * FROM kunde WHERE nachname = ... ORDER BY email ASC
    fun findByNachnameOrderByEmailAsc(nachname: String): Flux<Kunde>

    /**
     * Suche nach Kunden mit der gegebenen Postleitzahl.
     * @param plz Die gemeinsame Postleitzahl der gesuchten Kunden.
     * @return Die gefundenen Kunden als Flux.
     */
    // https://stackoverflow.com/questions/51646917/sonarqube-how-to-suppress-a-warning-in-kotlin-code#answer-51659699
    // SELECT * FROM kunde JOIN adresse ON ... WHERE plz = ...
    @Suppress("FunctionNaming", "kotlin:S100")
    fun findByAdresse_Plz(plz: String): Flux<Kunde>

    /**
     * Suche nach Kunden mit einem bestimmten Präfix für deren Nachnamen.
     * @param prefix Der Präfix für den Nachnamen.
     * @return Die gefundenen Kunden als Flux.
     */
    fun findByNachnameStartingWithIgnoreCase(prefix: String): Flux<Kunde>

    /**
     * Suche nach Kunden mit einem bestimmten Präfix für deren Emailadresse.
     * @param prefix Der Präfix für den Nachnamen.
     * @return Die gefundenen Kunden als Flux.
     */
    fun findByEmailStartingWithIgnoreCase(prefix: String): Flux<Kunde>

    /**
     * Kunde mit einer bestimmten Emailadresse in der DB löschen.
     * @param email Die Emailadresse.
     * @return Die Anzahl der gelöschten Kunden.
     */
    fun deleteByEmail(email: String): Mono<Long>
}
