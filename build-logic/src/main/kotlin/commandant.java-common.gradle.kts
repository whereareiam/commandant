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

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    add("compileOnly", libs.findLibrary("lombok").get())
    add("annotationProcessor", libs.findLibrary("lombok").get())

    add("compileOnly", libs.findLibrary("jetbrains-annotations").get())
    add("compileOnly", libs.findLibrary("adventure-api").get())
    add("compileOnly", libs.findLibrary("cloud-core").get())
    add("compileOnly", libs.findLibrary("cloud-minecraft-extras").get())
    add("compileOnly", libs.findLibrary("keystone").get())
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
