plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "pro.udeedit.devtools.cushystorage.demo"

    // Downgraded to 35 for maximum compatibility with existing projects
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "pro.udeedit.devtools.cushystorage.demo"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 4
        versionName = "1.1.0 lib 1.0.3"

        // vc3 (1.0.3) - Stable Release (Compatible & Test-Safe)
        //    - add branded launcher icon to demo app

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    /**
     * Modern configuration for Kotlin 2.x.
     * This block replaces the deprecated 'kotlinOptions' to set the JVM target.
     */
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Connects the app to the CushyStorage library module
    implementation(project(":cushy-storage"))
}
