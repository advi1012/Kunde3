/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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

package de.hska.kunde.config.dev

import de.hska.kunde.config.Settings.DEV
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice,
 * falls das Profile _dev_ aktiviert ist.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Configuration
@Profile(DEV)
class DevConfig :
        DbDeleteFiles,
        DbPopulate,
        LogBasicAuth,
        LogPasswordEncoding,
        LogRequest,
        MongoMappingEventsListener
