plugins {
    id("commandant.java-common")
    `java-test-fixtures`
    // Exports the defaults of CommandantMessages into translations/en/ for translators, and checks they are current.
    alias(libs.plugins.semantica)
}

dependencies {
    api(libs.keystone.api)

    // Commandant's own texts, declared in CommandantMessages: their implementation and their English defaults
    // (META-INF/semantica/en/commandant.json) are written when it compiles.
    annotationProcessor(libs.semantica.processor)

    // A real Keystone messenger over test senders, with Semantica reading Commandant's defaults, for every module's tests
    testFixturesApi(libs.keystone.api)
    testFixturesApi(libs.cloud.core)
    testFixturesApi(libs.semantica.testing)
    testFixturesImplementation(libs.keystone.common)
    testFixturesImplementation(libs.keystone.platform.common)
    testFixturesImplementation(libs.semantica.source.file)
    testFixturesImplementation(libs.semantica.syntax.icu)
    testFixturesImplementation(libs.semantica.format.adventure.v5)
    testFixturesImplementation(libs.configura)
    testFixturesImplementation(libs.adventure.api)
    testFixturesImplementation(libs.adventure.minimessage)
    testFixturesImplementation(libs.adventure.plain)
    testFixturesCompileOnly(libs.jetbrains.annotations)
    testFixturesCompileOnly(libs.lombok)
    testFixturesAnnotationProcessor(libs.lombok)

    testImplementation(testFixtures(project(":commandant-api")))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.cloud.core)
    testRuntimeOnly(libs.junit.platform)
}

// Test fixtures are for Commandant's own tests; they are not published.
val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["testFixturesApiElements"]) { skip() }
javaComponent.withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) { skip() }

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "commandant-api"
            pom {
                name.set("commandant-api")
                description.set("Public API for Commandant")
            }
        }
    }
}
