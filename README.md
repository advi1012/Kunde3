# Hinweise zum Programmierbeispiel

<Juergen.Zimmermann@HS-Karlsruhe.de>

> Diese Datei ist in Markdown geschrieben und kann z.B. mit IntelliJ IDEA
> oder NetBeans gelesen werden. Näheres zu Markdown gibt es in einem
> [Wiki](http://bit.ly/Markdown-Cheatsheet)

## Powershell

Überprüfung, ob sich Skripte (s.u.) starten lassen:

```CMD
    Get-ExecutionPolicy -list
```

Ggf. das Ausführungsrecht ergänzen:

```CMD
    Set-ExecutionPolicy RemoteSigned CurrentUser
```

## Falls die Speichereinstellung für Gradle zu großzügig ist

In `gradle.properties` bei `org.gradle.jvmargs` den voreingestellten Wert
(1,5 GB) ggf. reduzieren.

## Vorbereitungen im Quellcode der Microservices

### Eigener *aufgerufener* Microservice ("Server")

In `build.gradle` muss innerhalb von `runtime(` die Zeile
mit `'org.springframework.boot:spring-boot-starter-actuator',` auskommentiert
werden.

In `config\AppConfig.kt` ist folgende Annotationen zusätzlich
erforderlich `@EnableCircuitBreaker`.

In `Application.kt` sind folgende Properties zusätzlich erforderlich
(siehe Beispiel _kunde_):

* `eureka.`...
* `feign.`...

In `src\main\resources` ist zusätzlich die Konfigrationsdatei `bootstrap.yml`
für _Netflix Eureka_ und _Spring Config_ erforderlich.

### Eigener *aufrufender* Microservice ("Client")

In `build.gradle` muss innerhalb von `runtime(` die Zeile
mit `'org.springframework.boot:spring-boot-starter-actuator',` auskommentiert
werden.

In `config\AppConfig.kt` sind folgende Annotationen zusätzlich
erforderlich:

* `@EnableCircuitBreaker`

In `Application.kt` sind folgende Properties zusätzlich erforderlich
(siehe Beispiel _bestellung_):

* `eureka.`...
* `feign.`...

In `src\main\resources` ist zusätzlich die Konfigrationsdatei `bootstrap.yml`
für _Netflix Eureka_ und _Spring Config_ erforderlich.

## Vorbereitung für den Start der Server

### Internet-Verbindung

Eine _Internet-Verbindung_ muss vorhanden sein, damit die eigenen Microservices
über die IP-Adresse des Rechners aufgerufen werden können. Ansonsten würden die
Rechnernamen verwendet werden, wozu ein DNS-Server benötigt wird.

### IP-Adresse und hosts

Die IP-Adresse wird über das Kommando `ipconfig` ermittelt und liefert z.B.
folgende Ausgabe:

```TXT
    C:\>ipconfig

    Windows-IP-Konfiguration

    Ethernet-Adapter Ethernet:

       ...
       IPv4-Adresse  . . . . . . . . . . : 193.196.84.110
       ...
```

Die IP-Adresse muss dann in `C:\Windows\System32\drivers\etc\hosts` am
Dateiende eingetragen und abgespeichert werden. Dazu muss man
Administrator-Berechtigung haben.

```TXT
    193.196.84.110 localhost
```

### VirtualBox ggf. deaktivieren

Falls VirtualBox installiert ist, darf es nicht aktiviert sein, weil sonst
intern die IP-Adresse `192.168.56.1` verwendet wird.

VirtualBox wird folgendermaßen deaktiviert:

* Netzwerk- und Freigabecenter öffnen, z.B. Kontextmenü beim WLAN-Icon
* _"Adaptereinstellungen ändern"_ anklicken
* _"VirtualBox Host-only Network"_ anklicken
* Deaktivieren


### Proxy-Einstellung für Gradle

Die Proxy-Einstellung in gradle.properties muss richtig gesetzt sein. Dabei
muss die eigene IP-Adresse bei den Ausnahmen ("nonProxyHosts") eingetragen
sein, wozu man typischerweise Wildcards benutzt.

## Überblick: Start der Server

* MongoDB
* Service Discovery
* Config
* Zookeeper
* Kafka
* Mailserver
* Kafka-Receiver
* API-Gateway
* Zipkin
* Circuit Breaker Dashboard (_optional_)
* kunde
* bestellung

Die Server (außer MongoDB, Zookeeper und Kafka) sind jeweils in einem eigenen Gradle-Projekt.

## MongoDB starten und beenden

Durch Aufruf der .ps1-Datei:

````CMD
    .\mongodb.ps1
````

bzw.

````CMD
    .\mongodb.ps1 stop
````
s
## Mailserver

_FakeSMTP_ wird durch die .ps1-Datei `mailserver` gestartet und läuft auf Port 25000.

## Config-Server starten

Siehe `ReadMe.md` im Beispiel `config`.

Zusätzlich in `git\kunde-dev.properties` die Zeilen mit `server.http2.` und
`server.ssl.` auskommentieren, so dass der aufgerufene Microservice mit _http_
statt http/2 läuft.

## Übersetzung und Ausführung

### Start des Servers in der Kommandozeile

In einer Powershell wird der Server mit der Möglichkeit für einen
_Restart_ gestartet, falls es geänderte Dateien gibt:

```CMD
    .\kunde.ps1
```

### Start des Servers innerhalb von IntelliJ IDEA

Im Auswahlmenü rechts oben, d.h. dort wo _Application_ steht, die erste Option _Edit Configurations ..._ auswählen.
Danach beim Abschnitt _Environment_ im Unterabschnitt _VM options_ den Wert `-Djavax.net.ssl.trustStore=C:/Users/MEINE_KENNUNG/IdeaProjects/kunde/src/main/resources/truststore.p12 -Djavax.net.ssl.trustStorePassword=zimmermann` eintragen, wobei `MEINE_KENNUNG` durch die eigene Benutzerkennung
zu ersetzen ist. Nun beim Abschnitt _Spring Boot_ im Unterabschnitt _Active Profiles_ den Wert `dev` eintragen und
mit dem Button _OK_ abspeichern.

Von nun an kann man durch Anklicken des grünen Dreiecks rechts oben die _Application_ bzw. den Microservice starten.

### Kontinuierliches Monitoring von Dateiänderungen

In einer zweiten Powershell überwachen, ob es Änderungen gibt, so dass
die Dateien für den Server neu bereitgestellt werden müssen; dazu gehören die
übersetzten .class-Dateien und auch Konfigurationsdateien. Damit nicht bei jeder
Änderung der Server neu gestartet wird und man ständig warten muss, gibt es eine
"Trigger-Datei". Wenn die Datei `restart.txt` im Verzeichnis
`src\main\resources` geändert wird, dann wird ein _Neustart des Servers_
ausgelöst und nur dann.

Die Powershell, um kontinuierlich geänderte Dateien für den Server
bereitzustellen, kann auch innerhalb der IDE geöffnet werden (z.B. als
_Terminal_ bei IntelliJ).

```CMD
    .\gradlew classes -t
```

### Eventuelle Probleme mit Windows

_Nur_ falls es mit Windows Probleme gibt, weil der CLASSPATH zu lang ist und
deshalb `java.exe` nicht gestartet werden kann, dann kann man auf die beiden
folgenden Kommandos absetzen:

```CMD
    .\gradlew assemble
    java -jar build/libs/kunde-1.0.jar --spring.profiles.active=dev `
         -Djavax.net.ssl.trustStore=./src/main/resources/truststore.p12 `
         -Djavax.net.ssl.trustStorePassword=zimmermann
```

Die [Dokumentation](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#executable-jar)
enthält weitere Details zu einer ausführbaren JAR-Datei bei Spring Boot

### Properties beim gestarteten Microservice _kunde_ überprüfen

*Funktioniert noch nicht mit Spring Cloud*

Mit der URI `https://localhost:8444/actuator/env` kann überprüft werden, ob der
Microservice _kunde_ die Properties vom Config-Server korrekt ausliest. Der
Response wird mit dem MIME-Type `application/vnd.spring-boot.actuator.v1+json`
zurückgegeben, welcher von einem Webbrowser i.a. nicht verstanden wird.

Die vom Config-Server bereitgestellten Properties sind bei
`"configService:file:///C:/Users/.../IdeaProjects/config/git-repo/kunde-dev.properties"`
zu finden.

Analog können bei Microservice `bestellung` die Properties überprüft werden:

* Der Port ist von `8444` auf `8445` zu ändern.
* Bei `"configService:file:///C:/Users/...` steht `bestellung-dev.properties`

### Registrierung bei _Service Discovery_ überprüfen

```URI
    http://localhost:8761/eureka/apps/kunde
```

### Spans mit _Zipkin_ visualisieren

Im Webbrowser aufrufen:

```URI
    http://localhost:9411
```

Nachdem mindestens 1 Request zu einem Microservice (z.B. _kunde_) abgesetzt wurde,
auf den Button *Find Traces* klicken. Danach sieht man die einzelnen Methodenaufrufe
als _Spans_.

### Herunterfahren in einer eigenen Powershell

*Funktioniert noch nicht mit Spring WebFlux*

```CMD
    .\kunde.ps1 stop
```

### API-Dokumentation

```CMD
    .\gradlew dokka
```

Dazu muss man mit dem Internet verbunden sein, _ohne_ einen Proxy zu benutzen.

### Eureka-Client für die Tests aktivieren

In `src\test\resources\rest-test.properties` die Zeile `eureka.client.enabled = false` auskommentieren

### Tests

Folgende Server müssen gestartet sein:

* MongoDB
* Service Discovery
* Config
* Zookeeper
* Kafka
* Kafka-Receiver
* Mailserver

```CMD
    .\gradlew test --fail-fast [--rerun-tasks]
    .\gradlew jacocoTestReport
```

### Zertifikat in Chrome importieren

Chrome starten und die URI `chrome://settings` eingeben. Danach `Zertifikate verwalten`
im Suchfeld eingeben und auf den gefundenen Link klicken. Jetzt den Karteireiter
_Vertrauenswürdige Stammzertifizierungsstellen_ anklicken und über den Button _Importieren_
die Datei `src\test\resources\certificate.cer` auswählen.

### Codeanalyse durch detekt und ktlint

```CMD
    .\gradlew ktlintCheck detektCheck
```

Für `detektCheck` muss man online sein.

## curl als REST-Client

Beispiel:

```CMD
   C:\Zimmermann\Git\mingw64\bin\curl --basic --user admin:p http://localhost:8444/00000000-0000-0000-0000-000000000001
```

## Dashboard für Service Discovery (Eureka)

```URI
    http://localhost:8761
```

## Anzeige im Circuit Breaker Dashboard (FEHLT)

```URI
    http://localhost:8762
```

Im Dashboard die URI für den zu beobachtenden Microservice eingeben, z.B.:

```URI
    http://admin:p@localhost:8081/actuator/hystrix.stream
```

Hier wird BASIC-Authentifizierung mit dem Benutzernamen 'admin' und mit dem
Passwort 'p' verwendet.

### Beachte

* Erst **nach dem ersten Request** des zu beobachtenden Microservice ist eine
  Anzeige zu sehen.
* Mit dem Microservice wird über _HTTP_, und nicht über _HTTPS_ kommuniziert,
  weil man sonst für _Hystrix_ noch einen _Truststore_ konfigurieren müsste.
  Das würde den Umfang der Übungen sprengen und gehört in Vorlesungen mit den
  Schwerpunkten "IT-Sicherheit" und "Automatisierung von Geschäftsprozessen".
