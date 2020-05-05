import java.net.URI

plugins {
    id("com.driver733.gradle-kotlin-setup-plugin") version "1.1.3"
    `maven-publish`
    signing
    id("io.codearte.nexus-staging") version "0.21.2"
    id("de.marcphilipp.nexus-publish") version "0.4.0"
}

allprojects {
    repositories {
        mavenCentral()
    }

    group = "com.driver733.infix-fun-generator"
    version = rootProject.version

    apply<de.marcphilipp.gradle.nexus.NexusPublishPlugin>()

    nexusPublishing {
        repositories {
            sonatype()
        }
    }
}

nexusStaging {
    username = "driver733"
    password = "driver436398733"
    packageGroup = "com.driver733"
}

subprojects.filter { it.name != "example" }.onEach {

    with(it) {
        apply<SigningPlugin>()
        apply<MavenPublishPlugin>()
        apply<JavaPlugin>()

        tasks.withType(Javadoc::class) {
            setExcludes(setOf("**/*.kt"))
            options.encoding = "UTF-8"
        }

        configure<JavaPluginExtension> {
            withSourcesJar()
            withJavadocJar()
        }

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>(name) {
                    from(components["java"])
                    pom {
                        name.set("Infix functions generator")
                        description.set("Generation of infix functions for instance methods using annotations")
                        url.set("https://github.com/driver733/infix-functions-generator")
                        licenses {
                            license {
                                name.set("The MIT License")
                                url.set("http://www.opensource.org/licenses/mit-license.php")
                                distribution.set("repo")
                            }
                        }
                        issueManagement {
                            system.set("Github")
                            url.set("https://github.com/driver733/infix-functions-generator/issues")
                        }
                        developers {
                            developer {
                                id.set("driver733")
                                name.set("Mikhail Yakushin")
                                email.set("driver733@gmail.com")
                            }
                        }
                        scm {
                            connection.set("scm:git:git@github.com/driver733/infix-functions-generator.git")
                            developerConnection.set("scm:git:git@github.com/driver733/infix-functions-generator.git")
                            url.set("https://github.com/driver733/infix-functions-generator")
                        }
                    }
                }
            }

            repositories {
                maven {
                    credentials {
                        val ossSonatypeUsername: String? by project
                        val ossSonatypePassword: String? by project
                        username = ossSonatypeUsername
                        password = ossSonatypePassword
                    }
                    val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                    val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                    url = URI(if (version.toString().endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                }
            }
        }

        signing {
            val signingKeyId: String? by project
            val signingPassword: String? by project

            extra["signing.keyId"] = signingKeyId
            extra["signing.password"] = signingPassword
            extra["signing.secretKeyRingFile"] = "secrets/secring.gpg"

            sign(publishing.publications[name])
        }

        tasks.javadoc {
            if (JavaVersion.current().isJava9Compatible) {
                (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
            }
        }
    }
}.take(1).forEach { subproject ->

    tasks.getByPath(":${subproject.name}:publishToSonatype").apply {
        finalizedBy(
                ":closeRepository",
                ":releaseRepository"
        )
    }

    tasks.getByPath(":closeRepository").apply {
        mustRunAfter(subprojects.map { ":${it.name}:publishToSonatype" })
    }

    tasks.getByPath(":releaseRepository").apply {
        mustRunAfter(":closeRepository")
    }

}
