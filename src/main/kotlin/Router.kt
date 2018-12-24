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
package de.hska.kunde

import de.hska.kunde.config.logger
import de.hska.kunde.config.security.AuthHandler
import de.hska.kunde.entity.Kunde
import de.hska.kunde.rest.KundeHandler
import de.hska.kunde.rest.KundeMultimediaHandler
import de.hska.kunde.rest.KundeStreamHandler
import de.hska.kunde.rest.KundeValuesHandler
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router

/**
 * Spring-Konfiguration mit der Router-Function für die REST-Schnittstelle.
 *
 * @author Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface Router {
    /**
     * Bean-Function, um das Routing mit _Spring WebFlux_ funktional zu
     * konfigurieren.
     *
     * @param handler Objekt der Handler-Klasse [KundeHandler] zur Behandlung
     *      von Requests.
     * @param streamHandler Objekt der Handler-Klasse [KundeStreamHandler]
     *      zur Behandlung von Requests mit Streaming.
     * @param multimediaHandler Objekt der Handler-Klasse [KundeMultimediaHandler]
     *      zur Behandlung von Requests mit multimedialen Daten.
     * @param valuesHandler Objekt der Handler-Klasse [KundeValuesHandler]
     *      zur Behandlung von Requests bzgl. einfachen Werten.
     * @param authHandler Objekt der Handler-Klasse [AuthHandler]
     *      zur Behandlung von Requests bzgl. Authentifizierung und Autorisierung.
     * @return Die konfigurierte Router-Function.
     */
    @Bean
    fun router(
        handler: KundeHandler,
        streamHandler: KundeStreamHandler,
        multimediaHandler: KundeMultimediaHandler,
        valuesHandler: KundeValuesHandler,
        authHandler: AuthHandler
    ) = router {
        // https://github.com/spring-projects/spring-framework/blob/master/...
        //       ..spring-webflux/src/main/kotlin/org/springframework/web/...
        //       ...reactive/function/server/RouterFunctionDsl.kt
        "/".nest {
            accept(MediaType.APPLICATION_JSON).nest {
                GET("/", handler::find)
                GET("/$idPathPattern", handler::findById)

                // fuer "Software Engineering" und Android
                GET("$nachnamePath/{$prefixPathVar}",
                        valuesHandler::findNachnamenByPrefix)
                GET("$emailPath/{$prefixPathVar}",
                        valuesHandler::findEmailsByPrefix)
                GET("$versionPath/$idPathPattern",
                        valuesHandler::findVersionById)
            }

            contentType(MediaType.APPLICATION_JSON).nest {
                POST("/", handler::create)
                PUT("/$idPathPattern", handler::update)
                PATCH("/$idPathPattern", handler::patch)
            }

            DELETE("/$idPathPattern", handler::deleteById)
            DELETE("/", handler::deleteByEmail)

            accept(MediaType.TEXT_EVENT_STREAM).nest {
                GET("/", streamHandler::findAll)
            }

            // fuer Spring Batch
            accept(MediaType.TEXT_PLAIN).nest {
                GET("/anzahl", valuesHandler::anzahlKunden)
            }
        }

        multimediaPath.nest {
            GET("/$idPathPattern", multimediaHandler::download)
            PUT("/$idPathPattern", multimediaHandler::upload)
        }

        "/auth".nest {
            GET("/rollen", authHandler::findEigeneRollen)
        }

        // ggf. weitere Routen: z.B. HTML mit ThymeLeaf, Mustache, FreeMarker
    }
            .filter { request, next ->
                logger.trace("Filter vor dem Aufruf eines Handlers: {}",
                        request.uri())
                next.handle(request)
            }

    @Suppress("MayBeConstant")
    companion object {
        /**
         * Name der Pfadvariablen für IDs.
         */
        val idPathVar = "id"

        private val idPathPattern = "{$idPathVar:${Kunde.ID_PATTERN}}"

        /**
         * Pfad für multimediale Dateien
         */
        val multimediaPath = "/multimedia"

        /**
         * Pfad für Authentifizierung und Autorisierung
         */
        val authPath = "/auth"

        /**
         * Pfad, um Nachnamen abzufragen
         */
        val nachnamePath = "/nachname"

        /**
         * Pfad, um Emailadressen abzufragen
         */
        val emailPath = "/email"

        /**
         * Pfad, um Versionsnummern abzufragen
         */
        val versionPath = "/version"

        /**
         * Name der Pfadvariablen, wenn anhand eines Präfix gesucht wird.
         */
        val prefixPathVar = "prefix"

        private val logger = logger()
    }
}
