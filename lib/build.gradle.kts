import java.net.URI

plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "io.github.newagewriter"
version = project.extra["lib_version"] as String

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.newagewriter"
            artifactId = "kt-generator"
            version = project.extra["lib_version"] as String

            from(components["java"])
            pom {
                name = "kt-generator"
                description = "Kotlin file generator from template"
                url = "hhttps://github.com/newagewriter/kt-generator"
//                properties = mapOf(
//                    "myProp" to "value",
//                    "prop.with.dots" to "anotherValue"
//                )
                licenses {
                    license {
                        name = "The MIT License (MIT)"
                        url = "https://mit-license.org/"
                    }
                }
                developers {
                    developer {
                        id = "newagewriter"
                        name = "Krzysztof Betlej"
                        email = "pisarzenowejery@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git@github.com:newagewriter/kt-generator.git"
                    developerConnection = "scm:git:ssh://git@github.com:newagewriter/kt-generator.git"
                    url = "https://github.com/newagewriter/kt-generator"
                }
            }
        }
    }

    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = "https://maven.pkg.github.com/newagewriter/mapper"
//            credentials {
//                username = System.getenv("GITHUB_ACTOR")
//                password = System.getenv("GITHUB_TOKEN")
//            }
//        }

        maven {
            name = "MavenCentral"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = URI.create(if ((version as String).endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
//                username = System.getenv("JRELEASER_NEXUS2_USERNAME")
//                password = System.getenv("JRELEASER_NEXUS2_PASSWORD")
                username = "qaKUO9l+"
                password = "x4HL68w1YuvatQG8zmomCD6I0VWlMY2qVKbYqHy14BoF"
            }
        }
    }
}

repositories {
    mavenCentral()
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(kotlin("stdlib"))
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jsr223:1.8.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}