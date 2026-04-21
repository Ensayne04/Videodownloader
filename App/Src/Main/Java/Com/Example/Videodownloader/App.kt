package com.example.videodownloader

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize the download engine and FFmpeg on app startup
            YoutubeDL.getInstance().init(this)
            FFmpeg.getInstance().init(this)
        } catch (e: YoutubeDLException) {
            Log.e("AppInit", "Failed to initialize YoutubeDL", e)
        }
    }
}
