plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.finflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finflow"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Move resourceConfigurations inside defaultConfig where it belongs
        resourceConfigurations += listOf("en")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Fix for ParseLibraryResourcesTask error
    buildFeatures {
        buildConfig = true
    }

    // Prevent resource conflicts between libraries
    packaging {
        resources {
            excludes += listOf(
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

// Add resolution strategy for dependency conflicts
dependencies {
    // Core Android dependencies - using stable versions to avoid conflicts
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Using a stable ViewPager2 version to avoid conflicts
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

// Optional: Add this configuration if you continue to have dependency issues
configurations.all {
    resolutionStrategy {
        // Force specific versions if needed
        // force("androidx.core:core-ktx:1.12.0")
    }
}