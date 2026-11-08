plugins {
    id("commandant.java-common")
}

dependencies {
    api(project(":commandant-api"))

    compileOnly(libs.cloud.cooldowns)
    compileOnly(libs.cloud.annotations)

    testImplementation(libs.adventure.api)
    testImplementation(libs.cloud.core)
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly(libs.junit.platform)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "commandant-common"
            pom {
                name.set("commandant-common")
                description.set("Common implementation for Commandant")
            }
        }
    }
}
