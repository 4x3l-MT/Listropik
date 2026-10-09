plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.listropikmobile"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.listropikmobile"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Cliente HTTP para conectar con FastAPI
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Corrutinas para no congelar la pantalla
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
