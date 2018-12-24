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

//  Aufrufe
//  1) Microservice uebersetzen und starten
//        .\gradlew -t
//        .\gradlew compileKotlin
//        .\gradlew compileTestKotlin
//
//  2) Microservice als selbstausfuehrendes JAR erstellen und ausfuehren
//        .\gradlew bootJar
//        java -jar build/libs/....jar --spring.profiles.active=dev
//
//  3) Tests und QS
//        .\gradlew build
//        .\gradlew test [--rerun-tasks] [--fail-fast] jacocoTestReport
//        .\gradlew check -x test
//        .\gradlew ktlint detekt
//        .\gradlew cleanTest
//        .\gradlew sonarqube -x test
//
//  4) Sicherheitsueberpruefung durch OWASP Dependency Check
//        .\gradlew dependencyCheckAnalyze --info
//        .\gradlew dependencyCheckUpdate --info
//
//  5) Projektreport erstellen
//        .\gradlew projectReport
//        .\gradlew -q dependencyInsight --dependency spring-kafka
//        .\gradlew dependencies
//        .\gradlew dependencies --configuration runtimeOnly
//        .\gradlew buildEnvironment
//        .\gradlew htmlDependencyReport
//
//  6) Neue Abhaengigkeiten ueberpruefen
//        .\gradlew dependencyUpdates
//        .\gradlew versions
//
//  7) API-Dokumentation erstellen (funktioniert NICHT mit proxy.hs-karlruhe.de, sondern nur mit proxyads)
//        .\gradlew dokka
//
//  8) Report ueber die Lizenzen der eingesetzten Fremdsoftware
//        .\gradlew generateLicenseReport
//
//  9) Entwicklerhandbuch in "Software Engineering" erstellen
//        .\gradlew asciidoctor asciidoctorPdf
//
//  10)Docker-Image
//        .\gradlew jib
//
//  11) Abhaengigkeitsgraph
//        .\gradlew generateDependencyGraph
//
//  12) Daemon abfragen und stoppen
//        .\gradlew --status
//        .\gradlew --stop
//
//  13) Verfuegbare Tasks auflisten
//        .\gradlew tasks
//
//  14) Properties auflisten
//        .\gradlew properties
//        .\gradlew dependencyManagementProperties
//
//  15) Hilfe einschl. Typinformation
//        .\gradlew help --task bootRun
//
//  16) Initialisierung des Gradle Wrappers in der richtigen Version
//      (dazu ist ggf. eine Internetverbindung erforderlich)
//        gradle wrapper --gradle-version 5.1-milestone-1 --distribution-type=all

// https://github.com/gradle/kotlin-dsl/tree/master/samples
// https://docs.gradle.org/current/userguide/kotlin_dsl.html
// https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
// https://guides.gradle.org/migrating-build-logic-from-groovy-to-kotlin/

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.asciidoctor.gradle.jvm.AsciidoctorPdfTask
import org.gradle.plugins.ide.idea.model.IdeaModule
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

buildscript {
    dependencies {
        val kotlinVersion = "1.3.11"
        extra["kotlin.version"] = kotlinVersion
        classpath(kotlin("gradle-plugin", kotlinVersion))
        classpath(kotlin("allopen", kotlinVersion))

        val springBootVersion = "2.1.1.RELEASE"
        extra["spring-boot.version"] = springBootVersion
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
        
        //classpath("gradle.plugin.com.vanniktech:gradle-dependency-graph-generator-plugin:0.5.0")
    }
}

plugins {
    id("idea")
    id("jacoco")
    id("project-report")

    //val kotlinDslVersion = "1.0"
    //id("org.gradle.kotlin.kotlin-dsl.base") version kotlinDslVersion
    //id("org.gradle.kotlin.kotlin-dsl") version kotlinDslVersion
    //id("org.gradle.kotlin.embedded-kotlin") version kotlinDslVersion
    //id("org.gradle.kotlin.kotlin-dsl.compiler-settings") version kotlinDslVersion

    val kotlinVersion = "1.3.11"
    // FIXME https://youtrack.jetbrains.com/issue/KT-26808
    // https://dl.bintray.com/kotlin/kotlin-dev/org/jetbrains/kotlin/kotlin-compiler
    kotlin("jvm") version kotlinVersion
    // fuer Spring Beans und Mockito
    id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
    // fuer @ConfigurationProperties mit "data class"
    id("org.jetbrains.kotlin.plugin.noarg") version kotlinVersion
    // fuer spring-boot-configuration-processor
    //id("org.jetbrains.kotlin.kapt") version kotlinVersion
    
    id("io.spring.dependency-management") version "1.0.6.RELEASE"
    
    //val springBootVersion = "2.1.0.RELEASE"
    //extra["spring-boot.version"] = springBootVersion
    //id("org.springframework.boot") version springBootVersion

    // FIXME https://github.com/arturbosch/detekt/issues/1306
    // FIXME https://github.com/arturbosch/detekt/issues/1307
    // https://github.com/arturbosch/detekt/pull/1350
    id("io.gitlab.arturbosch.detekt") version "1.0.0-RC12"
    
    // http://redirect.sonarsource.com/doc/gradle.html
    id("org.sonarqube") version "2.6.2"

    id("org.jetbrains.dokka") version "0.9.17"
    
    // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin
    id("com.google.cloud.tools.jib") version "0.10.1"

    id("com.fizzpod.sweeney") version "4.0.0"

    // FIXME https://github.com/jeremylong/dependency-check-gradle/issues/104
    id("org.owasp.dependencycheck") version "4.0.0.1"

    val asciidoctorVersion = "2.0-alpha.5"
    id("org.asciidoctor.jvm.convert") version asciidoctorVersion
    id("org.asciidoctor.jvm.pdf") version asciidoctorVersion
    
    // https://github.com/vanniktech/gradle-dependency-graph-generator-plugin
    // FIXME https://github.com/nidi3/graphviz-java/issues/86
    // FIXME https://github.com/vanniktech/gradle-dependency-graph-generator-plugin/issues/77
    //id("com.vanniktech.dependency.graph.generator") version "0.5.0"

    // https://github.com/ben-manes/gradle-versions-plugin
    // https://github.com/ben-manes/gradle-versions-plugin/issues/263
    // https://github.com/ben-manes/gradle-versions-plugin/issues/270
    id("com.github.ben-manes.versions") version "0.20.0"

    // https://github.com/nwillc/vplugin
    id("com.github.nwillc.vplugin") version "2.2.2"
    //id("com.github.nwillc.vplugin") version "2.1.1"

    // https://github.com/intergamma/gradle-zap
    id("net.intergamma.gradle.gradle-zap-plugin") version "0.9.6"

    // https://github.com/jk1/Gradle-License-Report
    id("com.github.jk1.dependency-license-report") version "1.3"

    // https://github.com/jaredsburrows/gradle-license-plugin
    //id("com.jaredsburrows.license") version "0.8.42"
}

apply(plugin = "org.springframework.boot")
//apply(plugin = "com.vanniktech.dependency.graph.generator")

defaultTasks = listOf("bootRun")
group = "de.hska"
version = "1.0"

repositories {
    mavenCentral()
    //jcenter()
    //google()
    maven("http://dl.bintray.com/kotlin/kotlin-eap")
    // https://github.com/spring-projects/spring-framework/wiki/Spring-repository-FAQ
    // https://github.com/spring-projects/spring-framework/wiki/Release-Process
    maven("http://repo.spring.io/libs-milestone")
    //maven("http://repo.spring.io/libs-milestone-local")
    maven("http://repo.spring.io/release")
    // Snapshots von Spring Framework, Spring Data, Spring Security und Spring Cloud
    maven("http://repo.spring.io/libs-snapshot")
    // Development-Version von Mockito
    maven("https://dl.bintray.com/mockito/maven")
    // Snapshots von JaCoCo
    //maven("https://oss.sonatype.org/content/repositories/snapshots")
}

// -------------------------------------------------------------------------------------------
// Properties aus BOM-Dateien ueberschreiben
// siehe org.springframework.boot:spring-boot-dependencies
//    https://github.com/spring-projects/spring-boot/blob/master/spring-boot-dependencies/pom.xml
// siehe org.springframework.cloud:spring-cloud-dependencies
//    https://github.com/spring-cloud/spring-cloud-release/blob/master/spring-cloud-dependencies/pom.xml
// 5iehe org.springframework.cloud:spring-cloud-build-dependencies
//    https://github.com/spring-cloud/spring-cloud-build/blob/master/spring-cloud-build-dependencies/pom.xml
// siehe org.springframework.cloud:spring-cloud-netflix-dependencies
//    https://github.com/spring-cloud/spring-cloud-netflix/blob/master/spring-cloud-netflix-dependencies/pom.xml
// siehe org.springframework.cloud:spring-cloud-build
//    https://github.com/spring-cloud/spring-cloud-build/blob/master/pom.xml
// -------------------------------------------------------------------------------------------

//extra["aspectj.version"] = "1.9.2"
//extra["hibernate-validator.version"] = "6.0.13.Final"
//extra["jackson.version"] = "2.9.7"
//extra["javax-mail.version"] = "1.6.2"
//extra["javax-validation.version"] = "2.0.1.Final"
//extra["junit-jupiter.version"] = "5.3.2"
//extra["kafka.version"] = "2.0.1"
//extra["logback.version"] = "1.2.3"
extra["mockito.version"] = "2.23.9"
extra["mongo-driver-reactivestreams.version"] = "1.10.0"
extra["mongodb.version"] = "3.9.1"
extra["reactor-bom.version"] = "Californium-SR3"
//extra["slf4j.version"] = "1.7.25"
//extra["spring.version"] = "5.1.3.RELEASE"
//extra["spring-data-releasetrain.version"] = "Lovelace-SR3"
// Spring Integration verwendet Project Reactor
//extra["spring-integration.version"] = "5.1.1.RELEASE"
//extra["spring-kafka.version"] = "2.2.2.RELEASE"
//extra["spring-security.version"] = "5.1.2.RELEASE"
//extra["thymeleaf.version"] = "3.0.11.RELEASE"
//extra["tomcat.version"] = "9.0.13"
//extra["undertow.version"] = "2.0.16.Final"

// https://github.com/spring-cloud/spring-cloud-release/blob/master/pom.xml
extra["spring-cloud.version"] = "Greenwich.M3"
//extra["spring-cloud-commons.version"] = "2.1.0.M3"
//extra["spring-cloud-config.version"] = "2.1.0.M3"
//extra["spring-cloud-netflix.version"] = "2.1.0.M3"
// https://github.com/spring-cloud/spring-cloud-stream-starters/issues/52
//extra["spring-cloud-stream.version"] = "Fishtown.RC2"
//extra["spring.cloud.stream.binder.kafka.version"] = "2.1.0.RC2"
//extra["eureka.version"] = "1.9.8"
//extra["hystrix.version"] = "1.5.18"
//extra["ribbon.version"] = "2.3.0"

extra["brave.version"] = "5.5.2"

//val resilience4jVersion = "0.13.2"
val annotationsVersion = "16.0.3"
val paranamerVersion = "2.8"
//val springCloudStreamDependenciesVersion = "Fishtown.RC2"
val archUnitVersion = "0.9.3"
val jacocoVersion = "0.8.2"
val ktlintVersion = "0.29.0"
val intellijVersion = "2018.3"
val antJunitVersion = "1.10.5"
val owaspAutoupdate = false
val plantumlVersion = "1.2018.13"
val asciidoctorjVersion = "1.6.0-RC.2"
val asciidoctorjPdfVersion = "1.5.0-alpha.16"

// https://docs.gradle.org/current/userguide/upgrading_version_4.html#rel5.0:bom_import
dependencyManagement {
    // https://github.com/spring-cloud/spring-cloud-release/blob/master/docs/src/main/asciidoc/spring-cloud-starters.adoc#using-spring-cloud-dependencies-with-spring-io-platform
    //imports { mavenBom("io.projectreactor:reactor-bom:${extra["reactor-bom.version"]}") }
    //imports { mavenBom("org.junit:junit-bom:${extra["junit-jupiter.version"]}") }
    imports { mavenBom("io.zipkin.brave:brave-bom:${extra["brave.version"]}") }
    //imports { mavenBom("org.springframework.data:spring-data-releasetrain:${extra["spring-data-releasetrain.version"]}") }
    //imports { mavenBom("org.springframework.security:spring-security-bom:${extra["spring-security.version"]}") }
    //imports { mavenBom("org.springframework.integration:spring-integration-bom:${extra["spring-integration.version"]}") }
    //imports { mavenBom("org.springframework.boot:spring-boot-dependencies:${extra["spring-boot.version"]}") }
    //imports { mavenBom("org.springframework.cloud:spring-cloud-stream-dependencies:${springCloudStreamDependenciesVersion}") }
    imports { mavenBom("org.springframework.cloud:spring-cloud-dependencies:${extra["spring-cloud.version"]}") }
}

val plantumlCfg by configurations.creating
val ktlintCfg by configurations.creating

// https://docs.gradle.org/current/userguide/java_library_plugin.html#sec:java_library_separation
dependencies {
    // https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc#generating-your-own-metadata-by-using-the-annotation-processor
    //kapt("org.springframework.boot:spring-boot-configuration-processor")

    // https://youtrack.jetbrains.net/issue/KT-27463
    constraints {
        implementation("org.jetbrains:annotations:$annotationsVersion")
        kotlinCompilerClasspath("org.jetbrains:annotations:$annotationsVersion")
        runtimeOnlyDependenciesMetadata("org.jetbrains:annotations:$annotationsVersion")
        testRuntimeOnlyDependenciesMetadata("org.jetbrains:annotations:$annotationsVersion")
        ktlintCfg("org.jetbrains:annotations:$annotationsVersion")
        detekt("org.jetbrains:annotations:$annotationsVersion")
    }
    
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    
    // fuer YML-Konfigurationsdateien
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    // fuer Validierung von Methodenargumenten
    implementation("com.thoughtworks.paranamer:paranamer:$paranamerVersion")
    // fuer File-Upload mit WebFlux (statt Servlets)
    implementation("org.synchronoss.cloud:nio-multipart-parser")

    implementation("org.springframework.boot:spring-boot-starter-json")

    // CAVEAT: Falls spring-boot-starter-web als Dependency enthalten ist, wird Spring MVC konfiguriert,
    //         damit in MVC-Anwendungen der "reactive" WebClient nutzbar ist
    // spring-boot-starter-webflux enthaelt Reactor Netty als Default "Web Engine"
    // und org.hibernate.validator:hibernate-validator
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-tomcat")
    //implementation("org.springframework.boot:spring-boot-starter-undertow")
    //implementation("org.springframework.boot:spring-boot-starter-jetty")
    //implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure basiert auf SpringMVC, d.h. Servlets statt reactive
    implementation("org.springframework.boot:spring-boot-actuator")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    //implementation("com.hazelcast:hazelcast-spring")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("org.springframework.cloud:spring-cloud-starter-config")
    //implementation("org.springframework.cloud:spring-cloud-starter-consul-discovery")
    // FIXME https://github.com/x-stream/xstream/issues/101
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
        exclude(module = "woodstox-core-asl")
        exclude(module = "xmlpull")
        exclude(module = "xpp3_min")
        exclude(module = "jettison")
    }
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-hystrix")
    
    // https://github.com/resilience4j/resilience4j
    //implementation("io.github.resilience4j:resilience4j-spring-boot2:$resilience4jVersion")
	//implementation("io.github.resilience4j:resilience4j-reactor:$resilience4jVersion")
    
    // https://github.com/spring-cloud/spring-cloud-stream-starters/issues/52
    //implementation("org.springframework.cloud:spring-cloud-stream:2.1.0.RC2")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    //implementation("org.springframework.cloud:spring-cloud-starter-stream-rabbit")

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    //Sleuth with Zipkin via HTTP
    // org.springframework.cloud.sleuth.instrument.messaging.TraceMessagingAutoConfiguration
    implementation("org.springframework.cloud:spring-cloud-starter-zipkin")

    // https://www.vojtechruzicka.com/spring-boot-devtools
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
        exclude(module = "hamcrest-core")
        exclude(module = "hamcrest-library")
        exclude(module = "assertj-core")
        exclude(module = "json-path")
        exclude(module = "jsonassert")
        exclude(module = "xmlunit-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5-api:$archUnitVersion")
    
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:$archUnitVersion")
    //testRuntimeOnly("org.springframework.boot:spring-boot-starter-undertow")

    ktlintCfg("com.github.shyiko:ktlint:$ktlintVersion")

    plantumlCfg("net.sourceforge.plantuml:plantuml:$plantumlVersion")
    plantumlCfg("org.apache.ant:ant-junit:$antJunitVersion")
}

kotlin {
    experimental {
        //newInference = ENABLE
        //coroutines = ENABLE
    }
}

allOpen {
    annotation("org.springframework.stereotype.Component")
    annotation("org.springframework.stereotype.Service")
}

noArg {
    annotation("org.springframework.boot.context.properties.ConfigurationProperties")
}

sweeney {
    enforce(mapOf("type" to "gradle", "expect" to "[5,)"))
    // Sonar Scanner NumberFormatException: For input string: "12-ea"
    enforce(mapOf("type" to "jdk", "expect" to "[11.0.1,12)"))
    validate()
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            verbose = true
            freeCompilerArgs = listOf("-Xjsr305=strict")
            // ggf. wegen Kotlin-Daemon: %TEMP%\kotlin-daemon.* und %LOCALAPPDATA%\kotlin\daemon
            // https://youtrack.jetbrains.com/issue/KT-18300
            //  $env:LOCALAPPDATA\kotlin\daemon
            //  $env:TEMP\kotlin-daemon.<ZEITSTEMPEL>
            //allWarningsAsErrors = true
        }
        // https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc#generating-your-own-metadata-by-using-the-annotation-processor
        dependsOn(processResources)
    }
    
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "1.8"
            verbose = true
            freeCompilerArgs = listOf("-Xjsr305=strict")
            //allWarningsAsErrors = true
        }
    }

    // https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
    named<BootRun>("bootRun") {
        val args = ArrayList(jvmArgs).apply {
            add("-Dspring.profiles.active=dev")
            add("-Djavax.net.ssl.trustStore=${System.getProperty("user.dir")}/src/main/resources/truststore.p12")
            add("-Djavax.net.ssl.trustStorePassword=zimmermann")
            //add("-noverify")
            // Remote Debugger:   .\gradlew bootRun --debug-jvm
            //add("-verbose:class")
            //add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
        }
        setJvmArgs(args)
    }

    named<BootJar>("bootJar") {
        doLast {
            println("")
            println("Aufruf der ausfuehrbaren JAR-Datei:")
            println("java -jar build/libs/$archiveName --spring.profiles.active=dev")
            println("")
        }
    }

    test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")

            includeTags("rest", "multimediaRest", "streamingRest", "service")
            //includeTags("rest")
            //includeTags("multimediaRest")
            //includeTags("streamingRest")
            //includeTags("service")

            //excludeTags("service")
        }

        //filter {
        //    includeTestsMatching(includeTests)
        //}

        //systemProperty("java.util.logging.manager", "org.slf4j.bridge.SLF4JBridgeHandler")
        systemProperty("javax.net.ssl.trustStore", "./src/main/resources/truststore.p12")
        systemProperty("javax.net.ssl.trustStorePassword", "zimmermann")
        systemProperty("junit.platform.output.capture.stdout", true)
        systemProperty("junit.platform.output.capture.stderr", true)

        //damit nach den Tests immer ein HTML-Report von JaCoCo erstellt wird
        finalizedBy(jacocoTestReport)
    }
    
    jacoco {
        toolVersion = jacocoVersion
    }

    //jacocoTestReport {
    //    // Default: nur HTML-Report im Verzeichnis $buildDir/reports/jacoco
    //    // XML-Report fuer CI, z.B. Jenkins
    //    reports {
    //        xml.isEnabled = true
    //        html.isEnabled = true
    //    }
    //}
    getByName<JacocoReport>("jacocoTestReport") {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
        // afterEvaluate gibt es nur bei getByName<> ("eager"), nicht bei named<> ("lazy")
        // https://docs.gradle.org/5.0/release-notes.html#configuration-avoidance-api-disallows-common-configuration-errors
        afterEvaluate {
            classDirectories.setFrom(files(classDirectories.files.map {
                fileTree(it) { exclude("**/config/**", "**/entity/**") }
            }))
        }
    }

    // Docker-Image durch jib von Google
    // https://github.com/GoogleContainerTools/jib/tree/master/jib-gradle-plugin#example
    jib {
        from {
            image = "openjdk:alpine"
        }
        to {
            image = "my-docker-id/hska-${project.name}"
        }
    }

    // https://android.github.io/kotlin-guides/style.html
    // https://kotlinlang.org/docs/reference/coding-conventions.html
    // https://www.jetbrains.com/help/idea/code-style-kotlin.html
    // https://github.com/android/kotlin-guides/issues/37
    // https://github.com/shyiko/ktlint
    val ktlint by registering(JavaExec::class) {
        group = "verification"

        classpath = ktlintCfg
        main = "com.github.shyiko.ktlint.Main"
        //https://github.com/shyiko/ktlint/blob/master/ktlint/src/main/kotlin/com/github/shyiko/ktlint/Main.kt
        args = listOf(
            "--verbose",
            "--reporter=plain",
            "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml",
            "src/**/*.kt")
    }
    check { dependsOn(ktlint) }

    detekt {
        config = files(
                project.rootDir.resolve("config/detekt-default.yml"),
                project.rootDir.resolve("config/detekt.yml"))
        reports {
            xml {
                //enabled = true
                destination = file("build/reports/detekt.xml")
            }
            html {
                //enabled = true
                destination = file("build/reports/detekt.html")
            }

        }
        idea {
            path = "${System.getenv("USERPROFILE")}/.IntelliJIdea$intellijVersion"
            inspectionsProfile = "$projectDir/.idea/inspectionProfiles/Project_Default.xml"
        }
    }

    // http://stackoverflow.com/questions/34143530/sonar-maven-analysis-class-not-found#answer-34151150
    sonarqube {
        properties {
            // property("sonar.tests", "src/test/kotlin")
            // property("sonar.exclusions", "src/test/resources/truststore.p12")
            property("sonar.scm.disabled", true)
            // https://docs.sonarqube.org/display/SONAR/Authentication
            property("sonar.login", "admin")
            property("sonar.password", "admin")
        }
    }

    // https://github.com/jeremylong/DependencyCheck/blob/master/src/site/markdown/dependency-check-gradle/configuration.md
    // https://github.com/jeremylong/DependencyCheck/issues/360
    dependencyCheck {
        suppressionFile = "$projectDir/config/owasp.xml"
        //skipConfigurations = mutableListOf("ktlint", "detekt", "asciidoctor")
        data {
            directory = "C:/Zimmermann/owasp-dependency-check"
            username = "dc"
            password = "p"
        }

        format = org.owasp.dependencycheck.reporting.ReportGenerator.Format.ALL
    }

    val plantuml by registering {
        doLast {
            //https://github.com/gradle/kotlin-dsl/blob/master/samples/ant/build.gradle.kts
            ant.withGroovyBuilder {
                "taskdef"(
                        "name" to "plantuml",
                        "classname" to "net.sourceforge.plantuml.ant.PlantUmlTask",
                        "classpath" to plantumlCfg.asPath)

                // PNG-Bilder fuer HTML bei AsciiDoctor und Dokka
                mkdir("$buildDir/docs/images")
                "plantuml"(
                        "output" to "$buildDir/docs/images",
                        // "graphvizDot" to "C:\\Zimmermann\\Graphviz\\bin\\dot.exe",
                        "verbose" to true) {
                    "fileset"("dir" to "$projectDir/src/main/kotlin") {
                        "include"("name" to "**/*.puml")
                    }
                }

                // PNG-Bilder kopieren fuer AsciiDoctor mit dem IntelliJ-Plugin
                mkdir("$projectDir/config/images")
                "copy"("todir" to "$projectDir/config/images") {
                    "fileset"("dir" to "$buildDir/docs/images") {
                        "include"("name" to "*.png")
                    }
               }
            }
        }
    }

    named<DokkaTask>("dokka") {
        includes = listOf("Module.md")
        apiVersion = "1.3"
        languageVersion = apiVersion
        // http://docs.oracle.com/javase/11/docs/api gibt es nicht (ebenso fuer 10)
        jdkVersion = 9
        // https://kotlinlang.org/api/latest/jvm/stdlib/package-list
        // http://docs.oracle.com/javase/9/docs/api/package-list
        // FIXME https://github.com/Kotlin/dokka/issues/261
        // FIXME https://github.com/Kotlin/dokka/issues/188
        // https://github.com/Kotlin/dokka/commit/8e9e768d51d83c2ae1641a0c2d9ec4614c109117
        noStdlibLink = true

        dependsOn(plantuml)
    }

    // https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html
    //val dependencyGraph by registering(Exec::class) {
    //    //workingDir("../tomcat/bin")
    //    commandLine("dot", "$projectDir\\build\\reports\\dependency-graph\\dependency-graph.dot", "-Tpng", "-o", "$projectDir\\build\\dependency-graph.png")
    //}
    //val dependencyGraphPng by registering(Exec::class) {
    //    executable = "dot"
    //    setArgs(listOf("$buildDir\\dependency-graph.dot", "-Tpng", "-o", "$buildDir\\dependency-graph.png"))
    //    dependsOn(dependencyGraph)
    //}

    // https://github.com/asciidoctor/asciidoctor-gradle-plugin/tree/release_2_0_0_alpha_3
    named<AsciidoctorTask>("asciidoctor") {
        asciidoctorj {
            setVersion(asciidoctorjVersion)
            //requires("asciidoctor-diagram")
        }

        setSourceDir(file("config/docs"))
        //setOutputDir(file("$buildDir/docs/asciidoc"))
        logDocuments = true

        //attributes(mutableMapOf(
        //        "source-higlighter" to "coderay",
        //        "coderay-linenums-mode" to "table",
        //        "toc" to "left",
        //        "icon" to "font",
        //        "linkattrs" to true,
        //        "encoding" to "utf-8"))

        doLast {
            val separator = System.getProperty("file.separator")
            println("Das Entwicklerhandbuch ist in $buildDir${separator}docs${separator}asciidoc${separator}entwicklerhandbuch.html")
        }

        dependsOn(plantuml)
    }

    named<AsciidoctorPdfTask>("asciidoctorPdf") {
        asciidoctorj {
            setVersion(asciidoctorjVersion)
            setPdfVersion(asciidoctorjPdfVersion)
        }

        setSourceDir(file("config/docs"))
        //outputDir file("${buildDir}/docs/asciidocPdf")
        attributes(mutableMapOf("imagesdir" to "$buildDir/docs/images"))
        logDocuments = true

        doLast {
            val separator = System.getProperty("file.separator")
            println("Das Entwicklerhandbuch ist in $buildDir${separator}docs${separator}asciidoc${separator}entwicklerhandbuch.pdf")
        }

        dependsOn(plantuml)
    }
}

idea {
    module {
        setDownloadJavadoc(true)
    }
}
