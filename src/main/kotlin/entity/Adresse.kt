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

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Pattern

/**
 * Adressdaten für die Anwendungslogik und zum Abspeichern in der DB.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @property plz Die 5-stellige Postleitzahl als unveränderliches Pflichtfeld.
 * @property ort Der Ort als unveränderliches Pflichtfeld.
 * @constructor Erzeugt ein Objekt mit Postleitzahl und Ort.
 */
data class Adresse(
    @get:NotEmpty(message = "{adresse.plz.notEmpty}")
    @get:Pattern(regexp = "\\d{5}", message = "{adresse.plz}")
    val plz: String,

    @get:NotEmpty(message = "{adresse.ort.notEmpty}")
    val ort: String
)
