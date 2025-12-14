// build.gradle.kts (Module: app)

import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // HILT ve ROOM'un çalışması için bu iki eklenti ZORUNLUDUR
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.oyun"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.oyun" // Firebase ile uyumlu ID (Play Store için değiştirin)
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Load keystore properties from external file for security
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            } else {
                // Fallback for CI/CD or when keystore.properties doesn't exist
                println("WARNING: keystore.properties not found. Using environment variables or defaults.")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // Versiyon Tanımları
    val room_version = "2.6.1"
    val lifecycle_version = "2.6.2" // Daha güncel bir versiyon kullanıldı

    // --- TEMEL ANDROID KÜTÜPHANELERİ ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.code.gson:gson:2.10.1")


    // --- MİMARİ BİLEŞENLER (VIEWMODEL & LIFECYCLE) ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.activity:activity-ktx:1.8.2") // Daha güncel bir versiyon kullanıldı

    // --- HILT (BAĞIMLILIK ENJEKSİYONU) ---
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    // --- ROOM (VERİTABANI) ---
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    // --- FIREBASE (HIBRIT LIDERLIK TABLOSU IÇIN) ---
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx") // Crashlytics eklendi
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // 🔥 GOOGLE GİRİŞİ İÇİN YENİ EKLENEN KISIMLAR
    implementation("com.google.firebase:firebase-auth-ktx") // 1. Firebase Auth
    implementation("com.google.android.gms:play-services-auth:21.0.0") // 2. Google Girişi
    implementation("com.google.firebase:firebase-messaging-ktx") // 3. Firebase Cloud Messaging

    // 📱 ADMOB REKLAMLARI İÇİN
    implementation("com.google.android.gms:play-services-ads:22.6.0")

    // --- TEST KÜTÜPHANELERİ ---
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8") // MockK - Kotlin için mocking
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Coroutines test
    testImplementation("com.google.truth:truth:1.1.5") // Google Truth - Assertion library
    testImplementation("androidx.arch.core:core-testing:2.2.0") // LiveData/ViewModel test
    testImplementation("org.robolectric:robolectric:4.11.1") // Android framework test
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

// Hata ayıklamayı kolaylaştırmak için bu blok faydalıdır.
kapt {
    correctErrorTypes = true
}