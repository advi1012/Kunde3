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
package de.hska.kunde.config.security

import org.springframework.core.convert.converter.Converter
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.ReflectionUtils.findField
import org.springframework.util.ReflectionUtils.makeAccessible
import org.springframework.util.ReflectionUtils.setField

/**
 * Entity-Klasse, um Benutzerkennungen bestehend aus Benutzername,
 * Passwort und Rollen zu repräsentieren, die in MongoDB verwaltet
 * werden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @param id ID der Benutzerkennung iin MongoDB
 * @param username Benutzer- bzw. Loginname
 * @param password Passwort
 * @param authorities Berechtigungen bzw. Rollen
 *
 */
@Document(collection = "user")
@TypeAlias("CustomUserDetails")
class CustomUserDetails(
    val id: String?,
    username: String,
    password: String,
    authorities: Collection<SimpleGrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_KUNDE"))
) : User(username, password, authorities), UserDetails {
    /**
     * Konstruktor für Spring Data, weil die geerbten Java-Attribute `final` sind.
     * @param id ID in MongoDB
     * @param username Benutzer- bzw. Loginname
     * @param password Passwort
     * @param enabled Ist die Benutzerkennung schon benutzbar?
     * @param accountNonExpired Ist die Benutzerkennung noch nicht abgelaufen?
     * @param credentialsNonExpired ISt das Passwort noch nicht abgelaufen?
     * @param accountNonLocked Ist der Acccount nicht gesperrt?
     * @param authorities Berechtigungen bzw. Rollen
     */
    // Fuer Spring Data MongoDB: enabled, accoutNonExpired, ... aus der Klasse "User" sind private final
    @PersistenceConstructor constructor(
        id: String?,
        username: String,
        password: String,
        enabled: Boolean,
        accountNonExpired: Boolean,
        credentialsNonExpired: Boolean,
        accountNonLocked: Boolean,
        authorities: Collection<SimpleGrantedAuthority>
    ) : this(id = id, username = username, password = password, authorities = authorities) {
        setFinalField("enabled", enabled)
        setFinalField("accountNonExpired", accountNonExpired)
        setFinalField("credentialsNonExpired", credentialsNonExpired)
        setFinalField("accountNonLocked", accountNonLocked)
    }

    /**
     * Ein CustomUserDetails-Objekt als String, z.B. für Logging.
     * @return String mit den Properties.
     */
    override fun toString() =
        "CustomUserDetails(super=${super.toString()}, id='$id')"

    private fun setFinalField(fieldName: String, value: Any) {
        val field = findField(User::class.java, fieldName)!!
        makeAccessible(field)
        setField(field, this, value)
    }

    /**
     * Konvertierungsklasse für MongoDB, um einen String einzulesen und
     * eine Rolle als GrantedAuthority zu erzeugen. Wegen @ReadingConverter
     * ist kein Lambda-Ausdruck möglich.
     */
    @ReadingConverter
    class RoleReadConverter : Converter<String, GrantedAuthority> {
        /**
         * Konvertierung eines Strings in eine Rolle, d.h. GrantedAuthority,
         * wenn z.B. eine Rolle von einem persistenten Medium (hier: DB)
         * eingelesen wird.
         * @param role String mit einem Rollennamen.
         * @return Zugehöriges Objekt von SimpleGrantedAuthority
         */
        override fun convert(role: String) = SimpleGrantedAuthority(role)
    }

    /**
     * Konvertierungsklasse für MongoDB, um eine Rolle (GrantedAuthority)
     * in einen String zu konvertieren. Wegen @WritingConverter ist kein
     * Lambda-Ausdruck möglich.
     */
    @WritingConverter
    class RoleWriteConverter : Converter<GrantedAuthority, String> {
        /**
         * Konvertierung eines Objekts von GrantedAuthority in einen String,
         * z.B. beim Abspeichern.
         * @param grantedAuthority Objekt von GrantedAuthority
         * @return String z.B. zum Abspeichern.
         */
        override fun convert(grantedAuthority: GrantedAuthority) =
            grantedAuthority.authority
    }
}
