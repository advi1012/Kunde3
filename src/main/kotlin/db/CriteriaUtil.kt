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
package de.hska.kunde.db

import de.hska.kunde.config.logger
import de.hska.kunde.entity.FamilienstandType
import de.hska.kunde.entity.GeschlechtType
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.util.MultiValueMap

@Suppress("MayBeConstant")
/**
 * Singleton-Klasse, um _Criteria Queries_ für _MongoDB_ zu bauen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
object CriteriaUtil {
    private val nachnameStr = "nachname"
    private val emailStr = "email"
    private val kategorie = "kategorie"
    private val plz = "plz"
    private val plzField = "adresse.plz"
    private val ort = "ort"
    private val ortField = "adresse.ort"
    private val umsatzMin = "umsatzmin"
    private val umsatz = "umsatz.betrag"
    private val geschlecht = "geschlecht"
    private val familienstand = "familienstand"
    private val interessen = "interessen"
    private val anyStr = "\\.*"
    private val logger = logger()

    /**
     * Eine `MultiValueMap` von _Spring_ wird in eine Liste von
     * `CriteriaDefinition` für _MongoDB_ konvertiert, um flexibel nach Kunden
     * suchen zu können.
     * @param queryParams Die Query-Parameter in einer `MultiValueMap`.
     * @return Eine Liste von `CriteriaDefinition`.
     */
    @Suppress("ComplexMethod")
    fun getCriteria(queryParams: MultiValueMap<String, String>): List<CriteriaDefinition?> {
        val criteria = queryParams.map { (key, value) ->
            if (value?.size != 1) {
                null
            } else {
                val critVal = value[0]
                when (key) {
                    nachnameStr -> getCriteriaNachname(critVal)
                    emailStr -> getCriteriaEmail(critVal)
                    kategorie -> getCriteriaKategorie(critVal)
                    plz -> getCriteriaPlz(critVal)
                    ort -> getCriteriaOrt(critVal)
                    umsatzMin -> getCriteriaUmsatz(critVal)
                    geschlecht -> getCriteriaGeschlecht(critVal)
                    familienstand -> getCriteriaFamilienstand(critVal)
                    interessen -> getCriteriaInteressen(critVal)
                    else -> null
                }
            }
        }

        logger.debug("#Criteria: {}", criteria.size)
        criteria.forEach { logger.debug("Criteria: {}", it?.criteriaObject) }
        return criteria
    }

    private
    fun getCriteriaNachname(nachname: String): Criteria {
        logger.trace("Nachname: {}", nachname)
        // Suche nach Teilstrings ohne Gross-/Kleinschreibung
        return Criteria.where(nachnameStr)
                .regex("$anyStr$nachname$anyStr", "i")
    }

    private
    fun getCriteriaEmail(email: String): Criteria {
        logger.trace("Email: {}", email)
        // Suche ohne Gross-/Kleinschreibung
        return Criteria.where(emailStr).regex(email, "i")
    }

    private
    fun getCriteriaKategorie(kategorieStr: String): Criteria? {
        logger.trace("Kategorie: {}", kategorieStr)
        val kategorieVal = kategorieStr.toIntOrNull()
        return Criteria.where(kategorie).isEqualTo(kategorieVal)
    }

    private
    fun getCriteriaPlz(plz: String): Criteria {
        logger.trace("PLZ: {}", plz)
        // Suche mit Praefix
        return Criteria.where(plzField).regex("$plz$anyStr")
    }

    private
    fun getCriteriaOrt(ort: String): Criteria {
        logger.trace("Ort: {}", ort)
        // Suche nach Teilstrings ohne Gross-/Kleinschreibung
        return Criteria.where(ortField)
                .regex("$anyStr$ort${anyStr}i")
    }

    private
    fun getCriteriaUmsatz(umsatzStr: String): Criteria? {
        logger.trace("Umsatz: {}", umsatzStr)
        val umsatzVal = umsatzStr.toBigDecimalOrNull() ?: return null
        return Criteria.where(umsatz).gte(umsatzVal)
    }

    private
    fun getCriteriaGeschlecht(geschlechtStr: String): Criteria? {
        logger.trace("Geschlecht: {}", geschlechtStr)
        val geschlechtVal = GeschlechtType.build(geschlechtStr) ?: return null
        return Criteria.where(geschlecht).isEqualTo(geschlechtVal)
    }

    private
    fun getCriteriaFamilienstand(familienstandStr: String): Criteria? {
        logger.trace("Familienstand: {}", familienstandStr)
        val familienstandVal = FamilienstandType.build(familienstandStr) ?: return null
        return Criteria.where(familienstand).isEqualTo(familienstandVal)
    }

    @Suppress("ReturnCount")
    private
    fun getCriteriaInteressen(interessenStr: String): Criteria? {
        logger.trace("Interessen: {}", interessenStr)
        val interessenList = interessenStr
                .split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
        if (interessenList.isEmpty()) {
            return null
        }

        // Interessen mit "and" verknuepfen, falls mehr als 1
        val criteria = interessenList.asSequence().map {
            Criteria.where(interessen).isEqualTo(it)
        }.toMutableList()

        val firstCriteria = criteria[0]
        if (criteria.size == 1) {
            return firstCriteria
        }

        criteria.removeAt(0)
        @Suppress("SpreadOperator")
        return firstCriteria.andOperator(*criteria.toTypedArray())
    }
}
