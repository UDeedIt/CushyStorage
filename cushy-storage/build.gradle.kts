import org.gradle.plugins.signing.SigningExtension

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
    id("signing") // Ensure the signing plugin is applied
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
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
        // We only publish the 'release' variant
        singleVariant("release") {
            withSourcesJar()
            // withJavadocJar() // AGP handles this more directly in components["release"]
        }
    }
}

// To generate documentation in HTML
val dokkaHtmlJar by tasks.registering(Jar::class) {
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.named("dokkaGeneratePublicationHtml"))
    archiveClassifier.set("html-doc")
}

// To generate documentation in Javadoc
val javadocJar by tasks.registering(Jar::class) {
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.named("dokkaGeneratePublicationJavadoc"))
    archiveClassifier.set("javadoc")
}

// Correctly register javadocJar if you want to explicitly attach it.
// If you're using the Dokka plugin, the task name will be 'dokkaHtmlJar'.
// If you're using standard JavaDocs, it's typically 'javadocJar'.
// Let's assume you'll use Dokka for better Kotlin docs.
// You would need to add `id("org.jetbrains.dokka") version "1.9.10"` to your plugins block.

// If using Dokka:
//tasks.register("javadocJar", Jar::class) {
//    dependsOn(tasks.named("dokkaHtml")) // Depend on Dokka's HTML generation task
//    archiveClassifier.set("javadoc")
//    from(tasks.named("dokkaHtml")) // Output of the Dokka task
//}

// This task will create a sourcesJar file
//tasks.register("sourcesJar", Jar::class) {
//    archiveClassifier.set("sources")
//    from(android.sourceSets["main"].java.srcDirs)
//}

//// If NOT using Dokka (standard JavaDocs):
// tasks.register("javadocJar", Jar::class) {
//     archiveClassifier.set("javadoc")
//     from(tasks.javadoc) // Assuming a 'javadoc' task exists
// }

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
                            id.set("UDeedIt") // Your GitHub ID
                            name.set("Sargis Simonyan") // Your real name
                            email.set("YOUR_EMAIL_FOR_MAVEN@udeedit.pro") // Your email (public)
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/UDeedIt/CushyStorage.git")
                        developerConnection.set("scm:git:ssh://github.com:UDeedIt/CushyStorage.git")
                        url.set("https://github.com/UDeedIt/CushyStorage")
                    }
                }
            }
        }

        // --- Repository Configuration for Sonatype OSSRH ---
        repositories {
            maven {
                name = "OSSRH"
                // URL for Sonatype's staging repository
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    // Pulls username/password from gradle.properties
                    username = project.properties["ossrhUsername"] as String?
                    password = project.properties["ossrhPassword"] as String?
                }
            }
        }
    }

    // --- Signing Configuration ---
    // Explicitly configure the SigningExtension to apply the GPG signature
    project.extensions.configure(SigningExtension::class) {
        useGpgCmd() // This is important: tells Gradle to use the gpg executable
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