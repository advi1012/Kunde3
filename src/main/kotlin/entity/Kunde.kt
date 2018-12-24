/*
 * Copyright (C) 2013 - present Juergen Zimmermann, Hochschule Karlsruhe
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
package de.hska.kunde.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import de.hska.kunde.config.security.CustomUserDetails
import java.net.URL
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.Email
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Past
import javax.validation.constraints.Pattern
import org.hibernate.validator.constraints.UniqueElements
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Unveränderliche Daten eines Kunden. In DDD ist Kunde ist ein _Aggregate Root_.
 *
 * [Klassendiagramm](../../../../docs/images/Kunde.png)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @property id ID eines Kunden als UUID [ID_PATTERN]].
 * @property version Versionsnummer in der DB
 * @property nachname Nachname eines Kunden mit einem bestimmten Muster [NACHNAME_PATTERN].
 * @property email Email eines Kunden.
 * @property kategorie Kategorie eines Kunden mit Werten zwischen
 *      [MIN_KATEGORIE] und [MAX_KATEGORIE].
 * @property newsletter Flag, ob es ein Newsletter-Abo gibt.
 * @property geburtsdatum Das Geburtsdatum eines Kunden.
 * @property umsatz Der Umsatz eines Kunden.
 * @property homepage Die Homepage eines Kunden.
 * @property geschlecht Das Geschlecht eines Kunden.
 * @property familienstand Der Familienstand eines Kunden.
 * @property interessen Die Interessen eines Kunden.
 * @property adresse Die Adresse eines Kunden.
 * @property username Der Username bzw. Loginname eines Kunden.
 * @property _links HATEOAS-Links, wenn genau 1 JSON-Objekt in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 * @property links HATEOAS-Links, wenn ein JSON-Array in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 * @property user Das Objekt mit allen User-Daten (wird nicht in der DB
 *      gespeichert).
 */
@Document
@TypeAlias("Kunde")
@JsonPropertyOrder(
    "nachname", "email", "kategorie", "newsletter", "geburtsdatum",
    "umsatz", "homepage", "geschlecht", "familienstand", "interessen",
    "adresse", "user")
data class Kunde(
    @get:Pattern(regexp = ID_PATTERN, message = "{kunde.id.pattern}")
    @JsonIgnore
    val id: String?,

    @Version
    @JsonIgnore
    val version: Int? = null,

    @get:NotEmpty(message = "{kunde.nachname.notEmpty}")
    @get:Pattern(
        regexp = NACHNAME_PATTERN,
        message = "{kunde.nachname.pattern}")
    @Indexed
    val nachname: String,

    @get:NotEmpty(message = "{kunde.email.notEmpty}")
    @get:Email(message = "{kunde.email.pattern}")
    @Indexed(unique = true)
    val email: String,

    @get:Min(value = MIN_KATEGORIE, message = "{kunde.kategorie.min}")
    @get:Max(value = MAX_KATEGORIE, message = "{kunde.kategorie.max}")
    val kategorie: Int = 0,

    val newsletter: Boolean = false,

    @get:Past(message = "{kunde.geburtsdatum.past}")
    val geburtsdatum: LocalDate?,

    // "sparse" statt NULL bei relationalen DBen
    // Keine Indizierung der Kunden, bei denen es kein solches Feld gibt
    @Indexed(sparse = true)
    val umsatz: Umsatz? = null,

    val homepage: URL? = null,

    val geschlecht: GeschlechtType?,

    val familienstand: FamilienstandType? = null,

    @get:UniqueElements(message = "{kunde.interessen.uniqueElements}")
    val interessen: List<InteresseType>?,

    @get:Valid
    // @DBRef fuer eine eigenstaendige Collection
    //  auch fuer 1:N-Beziehunge, d.h. Attribute vom Typ List, Set, ...
    //  kein kaskadierendes save(), ...
    val adresse: Adresse,

    @Indexed(unique = true)
    val username: String? = null,

    @CreatedDate
    @JsonIgnore
    private val erzeugt: LocalDateTime? = null,

    @LastModifiedDate
    @JsonIgnore
    private val aktualisiert: LocalDateTime? = null
) {
    // Transiente Properties nicht im Konstruktor fuer Spring Data
    // var nicht val, da keine Initialisierung im Konstruktor
    @Suppress("PropertyName", "VariableNaming")
    @Transient
    var _links: Map<String, Map<String, String>>? = null

    @Transient
    var links: List<Map<String, String>>? = null

    @Transient
    var user: CustomUserDetails? = null

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Kunde-) Objekt die gleiche
     *      Emailadresse hat.
     */
    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kunde
        return email == other.email
    }

    /**
     * Hashwert aufgrund der Emailadresse.
     * @return Der Hashwert.
     */
    override fun hashCode() = email.hashCode()

    /**
     * Ein Kunde-Objekt als String, z.B. für Logging.
     * @return String mit den Properties.
     */
    override fun toString() = "Kunde(id=$id, version=$version, nachname=$nachname, email=$email, " +
        "kategorie=$kategorie, newsletter=$newsletter, " +
        "geburtsdatum=$geburtsdatum, umsatz=$umsatz, " +
        "homepage=$homepage, geschlecht=$geschlecht, " +
        "familienstand=$familienstand, interessen=$interessen, " +
        "adresse=$adresse, username=$username, erzeugt=$erzeugt, aktualisiert= $aktualisiert, _links=$_links, " +
        "links=$links, user=$user)"

    companion object {
        private const val HEX_PATTERN = "[\\dA-Fa-f]"
        /**
         * Muster für eine UUID.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"

        private const val NACHNAME_PREFIX = "o'|von|von der|von und zu|van"

        private const val NAME_PATTERN = "[A-ZÄÖÜ][a-zäöüß]+"

        /**
         * Muster für einen Nachnamen
         */
        const val NACHNAME_PATTERN = "($NACHNAME_PREFIX)?$NAME_PATTERN(-$NAME_PATTERN)?"

        /**
         * Maximaler Wert für eine Kategorie
         */
        const val MIN_KATEGORIE = 0L

        /**
         * Minimaler Wert für eine Kategorie
         */
        const val MAX_KATEGORIE = 9L
    }
}
