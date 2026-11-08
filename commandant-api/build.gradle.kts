plugins {
    id("commandant.java-common")
}

dependencies {
    api(libs.keystone)

    testImplementation(libs.adventure.api)
    testImplementation(libs.cloud.core)
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly(libs.junit.platform)
}

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
