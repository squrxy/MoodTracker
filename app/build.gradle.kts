plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.moodtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moodtracker"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Material 3 + базовые androidx
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment:1.8.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.airbnb.android:lottie:6.4.0")

    // MPAndroidChart (pie/doughnut)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lottie
    implementation("com.airbnb.android:lottie:6.4.0")

    // Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
