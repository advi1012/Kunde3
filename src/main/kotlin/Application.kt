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

package de.hska.kunde

import de.hska.kunde.config.Settings.banner
import de.hska.kunde.config.Settings.props
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.WebApplicationType.REACTIVE
import org.springframework.boot.context.ApplicationPidFileWriter

/**
 * Die Klasse, die beim Start des Hauptprogramms verwendet wird, um zu
 * konfigurieren, dass es sich um eine Anwendung mit _Spring Boot_ handelt.
 * Dadurch werden auch viele von Spring Boot gelieferte Konfigurationsklassen
 * automatisch konfiguriert.
 *
 * [Use Cases](../../../../docs/images/kunde.uc.png)
 *
 * [Komponentendiagramm](../../../../docs/images/kunde.comp.png)
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@SpringBootApplication
class Application

/**
 * Hauptprogramm, um den Microservice zu starten.
 *
 * @param args Evtl. zusätzliche Argumente für den Start des Microservice
 */
fun main(args: Array<String>) {
    @Suppress("SpreadOperator")
    runApplication<Application>(*args) {
        webApplicationType = REACTIVE
        setBanner(banner)
        setDefaultProperties(props)
        addListeners(ApplicationPidFileWriter())
    }
}
