plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "app.doggy.filamentsample"
  compileSdk = 34

  defaultConfig {
    applicationId = "app.doggy.filamentsample"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables {
      useSupportLibrary = true
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = "1.5.14"
  }

  packagingOptions {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  aaptOptions {
    noCompress("filamat")
  }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.20"))
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
  implementation("androidx.activity:activity-compose:1.9.0")
  implementation(platform("androidx.compose:compose-bom:2024.06.00"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")

  implementation("com.google.android.filament:filament-android:1.53.1")
  implementation("com.google.android.filament:filament-utils-android:1.53.1")
  implementation("com.google.android.filament:gltfio-android:1.53.1")
  implementation("com.google.android.filament:filamat-android:1.53.1")

  implementation("com.google.dagger:hilt-android:2.51.1")
  kapt("com.google.dagger:hilt-compiler:2.51.1")

  debugImplementation("androidx.compose.ui:ui-tooling")
}

kapt {
  correctErrorTypes = true
}

tasks.clean {
  doFirst {
    delete("src/main/assets/filamat")
  }
}
