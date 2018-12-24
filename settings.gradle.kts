pluginManagement {
    repositories {
        maven("http://dl.bintray.com/kotlin/kotlin-eap")
        maven("http://repo.spring.io/libs-milestone")
        maven("http://repo.spring.io/plugins-release")

        mavenCentral()
        //jcenter()

        gradlePluginPortal()
        //maven("https://plugins.gradle.org/m2")

        // Snapshots von Spring Framework, Spring Data, Spring Security und Spring Cloud
        //maven("http://repo.spring.io/libs-snapshot")
    }
}
rootProject.name = "kunde"
