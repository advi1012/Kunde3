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
package de.hska.kunde.db

import de.hska.kunde.db.KundeDbUtil.updateAdresse
import de.hska.kunde.db.KundeDbUtil.updateEmail
import de.hska.kunde.db.KundeDbUtil.updateFamilienstand
import de.hska.kunde.db.KundeDbUtil.updateGeburtsdatum
import de.hska.kunde.db.KundeDbUtil.updateGeschlecht
import de.hska.kunde.db.KundeDbUtil.updateHomepage
import de.hska.kunde.db.KundeDbUtil.updateInteressen
import de.hska.kunde.db.KundeDbUtil.updateKategorie
import de.hska.kunde.db.KundeDbUtil.updateNachname
import de.hska.kunde.db.KundeDbUtil.updateNewsletter
import de.hska.kunde.db.KundeDbUtil.updateUmsatz
import de.hska.kunde.entity.Adresse
import de.hska.kunde.entity.FamilienstandType
import de.hska.kunde.entity.GeschlechtType
import de.hska.kunde.entity.InteresseType
import de.hska.kunde.entity.Kunde
import de.hska.kunde.entity.Umsatz
import org.springframework.util.ReflectionUtils.findField
import org.springframework.util.ReflectionUtils.makeAccessible
import org.springframework.util.ReflectionUtils.setField
import java.net.URL
import java.time.LocalDate

// Hilfsfunktionalitaet fuer PUT und PATCH:
// Um eine Aenderung zu machen, muss ein Datensatz zuerst aus der DB ausgelesen
// werden. Danach ist er in einem Cache von Spring Data.
// Wenn man nun ein *NEUES* Objekt als Kopie im Hauptspeicher erstellt, das die
// zu aendernden Werte und die ID des in der DB vorhandenen Objekts enthaelt,
// dann gibt es im Cache *2* Objekte mit *GLEICHER ID*.
// Durch die Extension-Function koennen Properties bei einem Kunde-Objekt
// geaendert werden, das aus der DB ausgelesen wurde und *IMMUTABLE* (var) ist.
// Folglich gibt es im Cache nur *1* Objekt mit der zugehoerigen ID.

/**
 * Extension Function für [Kunde], um ein _immutable_ Objekt im Cache von
 * _Spring Data MongoDB_ modifizieren zu können.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
fun Kunde.update(other: Kunde) {
    // alle Werte bis auf ID und account setzen

    updateNachname(this, other.nachname)
    updateEmail(this, other.email)
    updateKategorie(this, other.kategorie)
    updateNewsletter(this, other.newsletter)
    updateGeburtsdatum(this, other.geburtsdatum)
    updateUmsatz(this, other.umsatz)
    updateHomepage(this, other.homepage)
    updateGeschlecht(this, other.geschlecht)
    updateFamilienstand(this, other.familienstand)
    updateInteressen(this, other.interessen)
    updateAdresse(this, other.adresse)
}

/**
 * Singleton-Klasse, um die Extension Function [Kunde.update] zu realisieren.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Suppress("TooManyFunctions")
object KundeDbUtil {
    private
    val clazz = Kunde::class.java

    private
    val nachnameProp = findField(clazz, "nachname")!!

    private
    val emailProp = findField(clazz, "email")!!

    private
    val kategorieProp = findField(clazz, "kategorie")!!

    private
    val newsletterProp = findField(clazz, "newsletter")!!

    private
    val geburtsdatumProp = findField(clazz, "geburtsdatum")!!

    private
    val umsatzProp = findField(clazz, "umsatz")!!

    private
    val homepageProp = findField(clazz, "homepage")!!

    private
    val geschlechtProp = findField(clazz, "geschlecht")!!

    private
    val familienstandProp = findField(clazz, "familienstand")!!

    private
    val interessenProp = findField(clazz, "interessen")!!

    private
    val adresseProp = findField(clazz, "adresse")!!

    init {
        makeAccessible(nachnameProp)
        makeAccessible(emailProp)
        makeAccessible(kategorieProp)
        makeAccessible(newsletterProp)
        makeAccessible(geburtsdatumProp)
        makeAccessible(umsatzProp)
        makeAccessible(homepageProp)
        makeAccessible(geschlechtProp)
        makeAccessible(familienstandProp)
        makeAccessible(interessenProp)
        makeAccessible(adresseProp)
    }

    /**
     * Den Nachnamen eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Nachnamen.
     * @param nachname Der neue Nachname.
     */
    fun updateNachname(kunde: Kunde, nachname: String) =
            setField(nachnameProp, kunde, nachname)

    /**
     * Die Emailadresse eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit der zu überschreibenden Emailadresse.
     * @param email Die neue Emailadresse.
     */
    fun updateEmail(kunde: Kunde, email: String) =
            setField(emailProp, kunde, email)

    /**
     * Die Kategorie eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit der zu überschreibenden Kategorie.
     * @param kategorie Die neue Kategorie.
     */
    fun updateKategorie(kunde: Kunde, kategorie: Int) =
            setField(kategorieProp, kunde, kategorie)

    /**
     * Das Flag für Newsletter eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Flag.
     * @param newsletter Das neue Flag für Newsletter.
     */
    fun updateNewsletter(kunde: Kunde, newsletter: Boolean) =
            setField(newsletterProp, kunde, newsletter)

    /**
     * Das Geburtsdatum eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Geburtsdatum.
     * @param geburtsdatum Das neue Geburtsdatum.
     */
    fun updateGeburtsdatum(kunde: Kunde, geburtsdatum: LocalDate?) =
            setField(geburtsdatumProp, kunde, geburtsdatum)

    /**
     * Den Umsatz eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Umsatz.
     * @param umsatz Der neue Umsatz.
     */
    fun updateUmsatz(kunde: Kunde, umsatz: Umsatz?) =
            setField(umsatzProp, kunde, umsatz)

    /**
     * Die URL für die Homepage eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit der zu überschreibenden Homepage.
     * @param homepage Die neue URL.
     */
    fun updateHomepage(kunde: Kunde, homepage: URL?) =
            setField(homepageProp, kunde, homepage)

    /**
     * Das Geschlecht eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Geschlecht.
     * @param geschlecht Das neue Geschlecht.
     */
    fun updateGeschlecht(kunde: Kunde, geschlecht: GeschlechtType?) =
            setField(geschlechtProp, kunde, geschlecht)

    /**
     * Den Familienstand eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit dem zu überschreibenden Familienstand.
     * @param familienstand Der neue Familienstand.
     */
    fun updateFamilienstand(kunde: Kunde, familienstand: FamilienstandType?) =
            setField(familienstandProp, kunde, familienstand)

    /**
     * Die Interessen eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit den zu überschreibenden Interessen.
     * @param interessen Die neuen Interessen.
     */
    fun updateInteressen(kunde: Kunde, interessen: List<InteresseType>?) =
            setField(interessenProp, kunde, interessen)

    /**
     * Die Adresse eines immutable Kunden überschreiben.
     * @param kunde Kunde-Objekt mit der zu überschreibenden Adresse.
     * @param adresse Die neue Adresse.
     */
    fun updateAdresse(kunde: Kunde, adresse: Adresse) =
            setField(adresseProp, kunde, adresse)
}
