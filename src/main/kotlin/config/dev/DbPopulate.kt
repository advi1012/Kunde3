@file:Suppress("StringLiteralDuplication")

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
package de.hska.kunde.config.dev

import com.mongodb.reactivestreams.client.MongoCollection
import de.hska.kunde.config.logger
import de.hska.kunde.entity.Kunde
import de.hska.kunde.entity.Kunde.Companion.ID_PATTERN
import de.hska.kunde.entity.Kunde.Companion.NACHNAME_PATTERN
import org.bson.Document
import org.slf4j.Logger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.data.domain.Range
import org.springframework.data.domain.Range.Bound
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.`object`
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.array
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.bool
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.date
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.int32
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import reactor.core.publisher.Mono

/**
 * Interface, um im Profil _dev_ die (Test-) DB neu zu laden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface DbPopulate {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen,
     * damit die (Test-) DB neu geladen wird.
     * @param mongoTemplate Template für MongoDB
     * @return CommandLineRunner
     */
    @Bean
    @Description("Test-DB neu laden")
    fun dbPopulate(mongoTemplate: ReactiveMongoTemplate) = CommandLineRunner {
        val logger = logger()
        logger.warn("Neuladen der Collection 'Kunde'")

        val logSuccess = { kunde: Kunde -> logger.warn("{}", kunde) }
        mongoTemplate.dropCollection<Kunde>()
            // Mono<Void>  ->  Mono<...>
            .then(createSchema(mongoTemplate))
            .flatMap { createIndex(mongoTemplate, logger) }
            // Mono  ->  Flux
            .thenMany(kunden)
            .flatMap(mongoTemplate::insert)
            .subscribe(logSuccess) { throwable ->
                logger.error(">>> EXCEPTION :")
                throwable.printStackTrace()
            }
    }

    @SuppressWarnings("MagicNumber")
    private fun createSchema(mongoTemplate: ReactiveMongoTemplate): Mono<MongoCollection<Document>> {
        val logger = logger()
        val maxKategorie = 9
        val plzLength = 5

        // https://docs.mongodb.com/manual/core/schema-validation
        // https://docs.mongodb.com/manual/release-notes/3.6/#json-schema
        // https://www.mongodb.com/blog/post/mongodb-36-json-schema-validation-expressive-query-syntax
        val schema = MongoJsonSchema.builder()
                .required("id", "nachname", "email")
                .properties(
                        string("id").matching(ID_PATTERN),
                        string("nachname").matching(NACHNAME_PATTERN),
                        string("email"),
                        int32("kategorie")
                                .within(Range.of(Bound.inclusive(0), Bound.inclusive(maxKategorie))),
                        bool("newsletter"),
                        date("geburtsdatum"),
                        `object`("umsatz")
                                .properties(string("betrag"), string("waehrung")),
                        string("homepage"),
                        string("geschlecht").possibleValues("M", "W"),
                        string("familienstand").possibleValues("L", "VH", "G", "VW"),
                        array("interessen").uniqueItems(true),
                        string("username"),
                        `object`("adresse")
                                .properties(string("plz").minLength(plzLength).maxLength(plzLength),
                                        string("ort")))
                .build()
        logger.info("JSON Schema fuer Kunde: {}", schema.toDocument().toJson())
        return mongoTemplate.createCollection<Kunde>(CollectionOptions.empty().schema(schema))
    }

    private fun createIndex(mongoTemplate: ReactiveMongoTemplate, logger: Logger) =
        createIndexKunde(mongoTemplate, logger)
                .flatMap { createIndexEmail(mongoTemplate, logger) }
                .flatMap { createIndexUmsatz(mongoTemplate, logger) }

    private fun createIndexKunde(mongoTemplate: ReactiveMongoTemplate, logger: Logger): Mono<String> {
        logger.warn("Index fuer 'nachname'")
        val idx = Index("nachname", ASC).named("nachname_idx")
        return mongoTemplate.indexOps<Kunde>().ensureIndex(idx)
    }

    private fun createIndexEmail(mongoTemplate: ReactiveMongoTemplate, logger: Logger): Mono<String> {
        logger.warn("Index fuer 'email'")
        val idx = Index("email", ASC).unique().named("email_idx")
        return mongoTemplate.indexOps<Kunde>().ensureIndex(idx)
    }

    private fun createIndexUmsatz(mongoTemplate: ReactiveMongoTemplate, logger: Logger): Mono<String> {
        logger.warn("Index fuer 'umsatz'")
        val idx = Index("umsatz", ASC).sparse().named("umsatz_idx")
        return mongoTemplate.indexOps<Kunde>().ensureIndex(idx)
    }
}
