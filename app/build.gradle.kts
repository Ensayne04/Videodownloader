plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.videodownloader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.videodownloader"
        minSdk = 24 // Required minimum for youtubedl-android to work well with ffmpeg
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.2")

    // WorkManager for background downloads
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // youtubedl-android (yt-dlp wrapper) & FFmpeg for audio/video merging
    implementation("com.github.yausername.youtubedl-android:library:0.17.0")
    implementation("com.github.yausername.youtubedl-android:ffmpeg:0.17.0")
}
    private fun hideProgress() {
        binding.progressBar.visibility = View.INVISIBLE
        binding.tvProgressText.visibility = View.INVISIBLE
        binding.progressBar.progress = 0
    }
}
