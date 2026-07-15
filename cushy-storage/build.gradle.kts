plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Publishing Block
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // These properties are pulled directly from your 'gradle.properties'
                groupId = project.property("LIBRARY_GROUP").toString()
                artifactId = project.property("LIBRARY_ARTIFACT_ID").toString()
                version = project.property("LIBRARY_VERSION").toString()

                // This attaches the compiled AAR and metadata to the publication
                from(components["release"])
            }
        }
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