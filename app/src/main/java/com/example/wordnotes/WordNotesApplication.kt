package com.example.wordnotes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.wordnotes.di.AppContainer

const val CHANNEL_ID = "com.example.wordnotes.CHANNEL_ID"

class WordNotesApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Reminder", NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}