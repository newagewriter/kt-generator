plugins {
    id("java")
    kotlin("jvm") version "1.8.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(mapOf("path" to ":lib")))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    project(":lib")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("Main")
}