import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.tasks.testing.Test

plugins {
    `java-library`
    `maven-publish`
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val buildVersion = providers.environmentVariable("VERSION").orElse("dev")

group = "me.whereareiam"
version = buildVersion.get()

// Java 21, as Keystone 2 and Semantica need it.
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

dependencies {
    add("compileOnly", libs.findLibrary("lombok").get())
    add("annotationProcessor", libs.findLibrary("lombok").get())

    add("compileOnly", libs.findLibrary("jetbrains-annotations").get())
    add("compileOnly", libs.findLibrary("cloud-core").get())
}

// A Java module name of its own, such as me.whereareiam.commandant.api, so that a NeoForge mod can nest the jar.
tasks.named<Jar>("jar") {
    val moduleName = "me.whereareiam." + project.name.replace("commandant-", "commandant.")
    manifest {
        attributes("Automatic-Module-Name" to moduleName)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

extensions.configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
        pom {
            url.set("https://github.com/whereareiam/commandant")
        }
    }

    repositories {
        maven {
            val base = providers.environmentVariable("PUBLISH_MAVEN_BASE_URL").orElse("https://registry.whereareiam.me/maven").get()
            val repository = providers.environmentVariable("PUBLISH_MAVEN_REPOSITORY").orElse("packages").get()
            url = uri("$base/$repository")

            credentials {
                username = providers.environmentVariable("PUBLISH_USER").orNull
                password = providers.environmentVariable("PUBLISH_TOKEN").orNull
            }
        }
    }
}
