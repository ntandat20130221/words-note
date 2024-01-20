package com.example.wordnotes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

const val CHANNEL_ID = "com.example.wordnotes.remider"

@HiltAndroidApp
class WordNotesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createRemindNotificationChannel()
    }

    private fun createRemindNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, getString(R.string.reminder), NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(false)
            vibrationPattern = longArrayOf(0)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}