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
package de.hska.kunde.rest

import de.hska.kunde.config.logger
import de.hska.kunde.Router.Companion.idPathVar
import de.hska.kunde.rest.util.contentType
import de.hska.kunde.service.KundeMultimediaService
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType.parseMediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyExtractors.toDataBuffers
import org.springframework.web.reactive.function.BodyExtractors.toMultipartData
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse
        .badRequest
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

/**
 * Eine Handler-Function wird von der Router-Function [de.hska.kunde.Router.router]
 * aufgerufen, nimmt einen Request entgegen und erstellt den Response.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @constructor Einen KundeMultimediaHandler mit einem injizierten
 *      [de.hska.kunde.service.KundeMultimediaService] erzeugen.
 */
@Component
class KundeMultimediaHandler(private val service: KundeMultimediaService) {
    /**
     * Eine miltimediale (binäre) Datei herunterladen.
     * @param request Das eingehende Request-Objekt mit der Kunde-ID als
     *      Pfadvariable.
     * @return Die multimediale Datei oder Statuscode 404, falls es keinen
     *      Kunden oder keine multimediale Datei gibt.
     */
    fun download(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)
        return service.findMedia(id)
                .flatMap {
                    val length = it?.contentLength() ?: 0
                    logger.trace("length = {}", length)
                    var contentType = it.contentType
                    logger.trace("contentType = {}", contentType)
                    // TODO https://youtrack.jetbrains.com/issue/IDEA-187630
                    if (contentType.contains('*')) {
                        contentType = "image/png"
                    }
                    val mediaType = parseMediaType(contentType)
                    logger.trace("mediaType={}", mediaType)
                    ok().contentLength(length)
                            .contentType(mediaType)
                            .body(it.toMono())
                }
                .switchIfEmpty(notFound().build())
    }

    /**
     * Eine multimediale (binäre) Datei hochladen.
     * @param request Der eingehende Request mit der Binärdatei im Rumpf oder als
     *      Teil eines Requests mit dem MIME-Typ `multipart/form-data`.
     * @return Statuscode 204 falls das Hochladen erfolgreich war oder 400 falls
     *      es ein Problem mit der Datei gibt.
     */
    fun upload(request: ServerRequest): Mono<ServerResponse> {
        val contentType = request.contentType() ?: return badRequest().build()
        logger.trace("contentType = {}", contentType)
        val id = request.pathVariable(idPathVar)

        return if (contentType.startsWith("multipart/form-data"))
            uploadMultipart(request, id)
        else uploadBinary(request, id, contentType)
    }

    // https://github.com/sdeleuze/webflux-multipart/blob/master/src/...
    //       ...main/java/com/example/MultipartRoute.java
    private fun uploadMultipart(request: ServerRequest, id: String) =
        request.body(toMultipartData())
                .flatMap {
                    val part = it.toSingleValueMap()["file"]
                    val contentType = part?.contentType()
                    logger.trace("contentType part = {}", contentType)
                    val content = part?.content() ?: Flux.empty()
                    save(content, id, contentType)
                }
                .switchIfEmpty(badRequest().build())

    private fun uploadBinary(request: ServerRequest, id: String, contentType: String): Mono<ServerResponse> {
        val data = request.body(toDataBuffers())
        return save(data, id, contentType)
    }

    private fun save(data: Flux<DataBuffer>, id: String, contentType: String?): Mono<ServerResponse> {
        if (contentType == null) {
            return badRequest().build()
        }

        // Flux<DataBuffer> als Mono<List<DataBuffer>>
        return data.collectList()
                .map { it[0] }
                .flatMap {
                    val inputStream = it?.asInputStream()
                    if (inputStream == null) {
                        null
                    } else {
                        service.save(inputStream, id, contentType)
                    }
                }
                .flatMap { noContent().build() }
                .switchIfEmpty(badRequest().build())
    }

    private companion object {
        val logger = logger()
    }
}
