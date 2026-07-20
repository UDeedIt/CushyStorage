import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    id("signing")
    alias(libs.plugins.kotlin.dokka)
//    alias(libs.plugins.dokka.javadoc)
}

android {
    namespace = "pro.udeedit.devtools.cushystorage"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // This block is crucial for newer AGP to correctly prepare sources and Javadoc
    // for publication. It automatically registers the necessary tasks.
    publishing {
        singleVariant("release") {
            withSourcesJar()
            // withJavadocJar() // AGP handles this more directly in components["release"]
        }
    }
}


/**
 * Task to create a JAR containing the Dokka-generated HTML.
 * This is a mandatory requirement for Maven Central publishing.
 */
val javadocJar by tasks.registering(Jar::class) {
    group = "publishing" // Categorizes the task in the Gradle menu
    description = "Assembles a JAR archive containing the Dokka documentation"

    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("dokka/html"))
    dependsOn("dokkaGenerate")
}

// Publishing Block
// Main publishing configuration for Maven Central
afterEvaluate {
    publishing {
        publications {
            // Define the Maven Publication for the 'release' variant
            create<MavenPublication>("release") {
                groupId = project.property("LIBRARY_GROUP").toString()
                artifactId = project.property("LIBRARY_ARTIFACT_ID").toString()
                version = project.property("LIBRARY_VERSION").toString()

                from(components["release"]) // Attach the compiled AAR

                // Attach the generated Javadoc and Source JARs
                artifact(tasks.named("javadocJar"))

                // --- POM Metadata for Maven Central ---
                pom {
                    name.set("CushyStorage")
                    description.set("A unified, ultra-comfortable Android storage library.")
                    url.set("https://github.com/UDeedIt/CushyStorage")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("UDeedIt")
                            name.set("Sargis Simonyan")
                            email.set("udeedit.pro@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/UDeedIt/CushyStorage.git")
                        developerConnection.set("scm:git:ssh://git@github.com/UDeedIt/CushyStorage.git")
                        url.set("https://github.com/UDeedIt/CushyStorage")
                    }
//                    scm {
//                        connection.set("scm:git:git://github.com/UDeedIt/CushyStorage.git")
//                        developerConnection.set("scm:git:ssh://github.com:UDeedIt/CushyStorage.git")
//                        url.set("https://github.com/UDeedIt/CushyStorage")
//                    }
                }
            }
        }

        // --- Repository Configuration for Sonatype OSSRH ---
//        repositories {
//            maven {
//                name = "OSSRH"
//                // URL for Sonatype's staging repository
//                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//                credentials {
//                    // Pulls username/password from gradle.properties
//                    username = project.properties["ossrhUsername"] as String?
//                    password = project.properties["ossrhPassword"] as String?
//                }
//            }
//        }

        // for manual upload to Maven Central
        repositories {
            maven {
                name = "Bundle"
                // This creates a folder inside your project build directory
                url = uri(layout.buildDirectory.dir("bundle"))
            }
        }

    }

    // --- Signing Configuration ---
//    project.extensions.configure(SigningExtension::class) {
//        // Use the actual GPG application on your Mac
//        useGpgCmd()
//        sign(publishing.publications["release"])
//    }

    // Explicitly configure the SigningExtension to apply the GPG signature
    project.extensions.configure(SigningExtension::class) {
        val secretKey = project.findProperty("signing.secretKey") as String?
        val password = project.findProperty("signing.password") as String?

        if (secretKey != null && password != null) {
            /**
             * CRUCIAL: We use useInMemoryPgpKeys.
             * This explicitly prevents Gradle from calling '/usr/local/bin/gpg'
             * and avoids the 'ioctl' error entirely.
             */
            useInMemoryPgpKeys(secretKey, password)
        } else {
            // Fallback for local development if keys aren't in properties
            useGpgCmd()
        }

        sign(publishing.publications["release"])
    }
}


dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // datastore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.kotlinx.coroutines.test.v180)
}