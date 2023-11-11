package com.example.wordnotes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.example.wordnotes.di.AppContainer

const val CHANNEL_ID = "com.example.wordnotes.remider"

class WordNotesApplication : Application() {
    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
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