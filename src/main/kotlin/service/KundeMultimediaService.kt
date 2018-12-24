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
package de.hska.kunde.service

import de.hska.kunde.config.logger
import de.hska.kunde.db.KundeRepository
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.io.InputStream
import java.time.Duration

/**
 * Anwendungslogik für multimediale Daten zu Kunden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Service
class KundeMultimediaService(private val repo: KundeRepository, private val gridFsTemplate: GridFsTemplate) {
    /**
     * Multimediale Datei (Bild oder Video) zu einem Kunden mit gegebener ID
     * ermitteln.
     * @param kundeId Kunde-ID
     * @return Multimediale Datei, falls sie existiert. Sonst empty().
     */
    fun findMedia(kundeId: String) =
        repo.existsById(kundeId)
                .timeout(timeout)
                .flatMap {
                    val file = if (it)
                        gridFsTemplate.getResource(kundeId)
                    else null
                    Mono.justOrEmpty(file)
                }

    /**
     * Multimediale Daten aus einem Inputstream werden persistent mit der
     * gegebenen Kunden-ID als Dateiname abgespeichert. Der Inputstream wird
     * am Ende geschlossen.
     *
     * @param inputStream Inputstream mit multimedialen Daten.
     * @param kundeId Kunde-ID
     * @param contentType MIME-Type, z.B. image/png
     * @return ID der neuangelegten multimedialen Datei
     */
    @Transactional
    fun save(inputStream: InputStream, kundeId: String, contentType: String) =
        repo.existsById(kundeId)
                .timeout(timeout)
                .flatMap {
                    // TODO MIME-Type ueberpruefen
                    logger.warn("TODO: MIME-Type ueberpruefen")
                    val idGridFS = if (it) {
                        // ggf. multimediale Datei loeschen
                        val criteria = Criteria.where("filename")
                                .isEqualTo(kundeId)
                        val query = Query(criteria)
                        gridFsTemplate.delete(query)

                        // store() schliesst auch den Inputstream
                        gridFsTemplate
                                .store(inputStream, kundeId, contentType)
                                .toHexString()
                    } else {
                        null
                    }

                    logger.trace("ID GridFS: {}", idGridFS)
                    idGridFS.toString().toMono()
                }

    private companion object {
        val logger = logger()
        @Suppress("MagicNumber", "HasPlatformType")
        val timeout = Duration.ofMillis(500)
    }
}
