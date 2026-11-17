plugins {
    id("commandant.java-common")
}

dependencies {
    api(project(":commandant-api"))
    implementation(project(":commandant-common"))

    api(libs.cloud.annotations)

    // Testing
    testRuntimeOnly(libs.junit.platform)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.cloud.cooldowns)
    testImplementation(libs.cloud.minecraft.extras)
    testImplementation(libs.adventure.api)
    testImplementation(libs.jetbrains.annotations)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "commandant"
            pom {
                name.set("commandant")
                description.set("Aggregator module for Commandant")
            }
        }
    }
}
