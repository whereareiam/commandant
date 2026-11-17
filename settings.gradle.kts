import org.gradle.api.initialization.resolve.RepositoriesMode

rootProject.name = "Commandant"

pluginManagement {
    includeBuild("build-logic")

    repositories {
        gradlePluginPortal()
        mavenCentral()
        // Semantica's Gradle plugin, a development build until it is published
        mavenLocal()
        maven("https://registry.whereareiam.me/maven/packages")
        maven("https://maven.whereareiam.me/release")
        maven("https://maven.whereareiam.me/development")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        mavenCentral()
        // Keystone 2 and Semantica's development builds, until they are published
        mavenLocal()
        maven("https://registry.whereareiam.me/maven/packages")
        maven("https://maven.whereareiam.me/release")
        maven("https://maven.whereareiam.me/development")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

include(":commandant-api")
project(":commandant-api").projectDir = file("commandant-api")

include(":commandant-common")
project(":commandant-common").projectDir = file("commandant-common")

include(":commandant")
project(":commandant").projectDir = file("commandant")
