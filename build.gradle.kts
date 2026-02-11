import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.22"
    id("org.jetbrains.dokka") version "1.7.20"
    id("org.sonarqube") version "5.0.0.4638"
    id("maven-publish")
    id("java-library")
    id("signing")
    id("jacoco")
    id("io.github.zenhelix.maven-central-publish") version "0.11.2"
}

group = "dev.scheenen"
version = "3.5.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

defaultTasks("assemble")

val javaVersion = "1.8"
val kotlinVersion = "1.7"

tasks.apply {
    jar {
        manifest {
            attributes(
                "Maven-Artifact-Name" to project.name,
                "Maven-Artifact-Group" to project.group,
                "Maven-Artifact-Version" to project.version
            )
        }
    }
}

// ================= PUBLISHING ================

java {
    withSourcesJar()
    withJavadocJar()
}

// Configure Dokka for javadocJar
tasks.named<Jar>("javadocJar") {
    from(tasks.named<DokkaTask>("dokkaJavadoc"))
}

publishing {
    repositories {
        mavenCentralPortal {
            credentials {
                username = findProperty("centralTokenUser") as String? ?: System.getenv("CENTRAL_TOKEN_USER")
                password = findProperty("centralTokenPass") as String? ?: System.getenv("CENTRAL_TOKEN_PASS")
            }
        }
    }

    publications {
        create<MavenPublication>("ippclient") {
            from(components["java"])

            pom {
                name = "ipp-client-kotlin"
                description = "A client implementation of the ipp protocol written in kotlin "
                url = "https://github.com/X-rays5/ipp-client-kotlin"

                licenses {
                    license {
                        name = "MIT"
                        url = "https://raw.githubusercontent.com/X-rays5/ipp-client-kotlin/refs/heads/master/LICENSE"
                    }
                }

                developers {
                    developer {
                        id = "X-rayS5"
                        name = "X-ray"
                        email = "61073708+X-rays5@users.noreply.github.com"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/X-rays5/ipp-client-kotlin.git"
                    developerConnection = "scm:git:git@github.com:X-rays5/ipp-client-kotlin.git"
                    url = "https://github.com/X-rays5/ipp-client-kotlin"
                }
            }
        }
    }
}

// Signing
signing {
    useGpgCmd()
    sign(publishing.publications)
}

// ================= SONARQUBE / JACOCO =================

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
}

tasks.sonar {
    dependsOn(tasks.jacocoTestReport)
}

val isRunningOnGithub = System.getenv("CI")?.toBoolean() ?: false
println("isRunningOnGithub=$isRunningOnGithub")
