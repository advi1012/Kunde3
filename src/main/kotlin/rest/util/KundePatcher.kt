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
package de.hska.kunde.rest.util

import de.hska.kunde.entity.InteresseType
import de.hska.kunde.entity.Kunde

/**
 * Singleton-Klasse, um PATCH-Operationen auf Kunde-Objekte anzuwenden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
object KundePatcher {
    /**
     * PATCH-Operationen werden auf ein Kunde-Objekt angewandt.
     * @param kunde Das zu modifizierende Kunde-Objekt.
     * @param operations Die anzuwendenden Operationen.
     * @return Ein Kunde-Objekt mit den modifizierten Properties.
     */
    fun patch(kunde: Kunde, operations: List<PatchOperation>): Kunde {
        val replaceOps = operations.filter { "replace" == it.op }
        var kundeUpdated = replaceOps(kunde, replaceOps)

        val addOps = operations.filter { "add" == it.op }
        kundeUpdated = addInteressen(kundeUpdated, addOps)

        val removeOps = operations.filter { "remove" == it.op }
        return removeInteressen(kundeUpdated, removeOps)
    }

    private fun replaceOps(kunde: Kunde, ops: Collection<PatchOperation>): Kunde {
        var kundeUpdated = kunde
        ops.forEach { (_, path, value) ->
            when (path) {
                "/nachname" -> {
                    kundeUpdated = replaceNachname(kundeUpdated, value)
                }
                "/email" -> {
                    kundeUpdated = replaceEmail(kundeUpdated, value)
                }
            }
        }
        return kundeUpdated
    }

    private fun replaceNachname(kunde: Kunde, nachname: String) = kunde.copy(nachname = nachname)

    private fun replaceEmail(kunde: Kunde, email: String) = kunde.copy(email = email)

    private fun addInteressen(kunde: Kunde, ops: Collection<PatchOperation>):
            Kunde {
        if (ops.isEmpty()) {
            return kunde
        }
        var kundeUpdated = kunde
        ops.filter { "/interessen" == it.path }
                .forEach { kundeUpdated = addInteresse(it, kundeUpdated) }
        return kundeUpdated
    }

    private fun addInteresse(op: PatchOperation, kunde: Kunde): Kunde {
        val interesseStr = op.value
        val interesse = InteresseType.build(interesseStr)
                ?: throw InvalidInteresseException(interesseStr)
        val interessen = if (kunde.interessen == null)
            mutableListOf()
        else kunde.interessen.toMutableList()
        interessen.add(interesse)
        return kunde.copy(interessen = interessen)
    }

    private fun removeInteressen(kunde: Kunde, ops: List<PatchOperation>): Kunde {
        if (ops.isEmpty()) {
            return kunde
        }
        var kundeUpdated = kunde
        ops.filter { "/interessen" == it.path }
                .forEach { kundeUpdated = removeInteresse(it, kunde) }
        return kundeUpdated
    }

    private fun removeInteresse(op: PatchOperation, kunde: Kunde): Kunde {
        val interesseStr = op.value
        val interesse = InteresseType.build(interesseStr)
                ?: throw InvalidInteresseException(interesseStr)
        val interessen = kunde.interessen?.filter { it != interesse }
        return kunde.copy(interessen = interessen)
    }
}

/**
 * Exception, falls bei einer PATCH-Operation ein ungültiger Wert für ein
 * Interesse verwendet wird.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
class InvalidInteresseException(value: String)
    : RuntimeException("$value ist kein gueltiges Interesse")
