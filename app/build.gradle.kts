plugins {
    // Volvemos a la sintaxis moderna de plugins
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // 1. CRÍTICO: Agregamos el plugin KAPT (alternativa a KSP)
    id("kotlin-kapt")
}

android {
    // 🚨 CORRECCIÓN 1: El namespace debe ser 'jqlqapa'
    namespace = "com.jqlqapa.appnotas"
    compileSdk = 36

    defaultConfig {
        // 🚨 CORRECCIÓN 2: El applicationId debe ser 'jqlqapa'
        applicationId = "com.jqlqapa.appnotas"
        // Mantengo minSdk 24 porque la mayoría de las librerías modernas de Compose lo exigen.
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Jetpack Compose ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.ui.text)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")


    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("io.coil-kt:coil-compose:2.7.0") // Para carga de imágenes/miniaturas

    // 2. SOLUCIÓN: Agregar dependencias de Íconos Extendidos de Compose
    implementation("androidx.compose.material:material-icons-extended")

    // --- ROOM (SQLite Persistence) ---
    val room_version = "2.7.0"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // 3. CRÍTICO: Reemplazamos KSP por KAPT para el compilador de Room
    // KAPT es una alternativa más antigua y estable que debería solucionar el error de referencia.
    kapt("androidx.room:room-compiler:$room_version")

    // --- Lifecycle y Coroutines ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // --- Tests ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}

// Función auxiliar para usar kapt en el bloque de dependencias
fun org.gradle.api.artifacts.dsl.DependencyHandler.kapt(dependency: String) {
    add("kapt", dependency)
}
