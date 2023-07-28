package com.example.wordnotes.ui.settings

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wordnotes.CHANNEL_ID
import com.example.wordnotes.R
import com.example.wordnotes.WordNotesApplication
import java.time.LocalTime

class RemindReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appContainer = (context.applicationContext as WordNotesApplication).appContainer
        val wordReminder = appContainer.wordReminderFactory.create()

        try {
            if (isTimeOut(appContainer.wordPreferencesFactory.create())) {
                wordReminder.schedule(next = true)
            } else {
                pushNotification(context)
            }
        } catch (_: Exception) {
            wordReminder.cancel()
        }
    }

    private fun pushNotification(context: Context) {
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setContentTitle("Hello world!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        val notificationManager = context.applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.notify(0, notification)
    }

    private fun isTimeOut(wordPreferences: WordPreferences): Boolean {
        val endTime = TimePickerPreference.Formatter.parse(wordPreferences.getEndTime()!!)
        return LocalTime.now().isAfter(endTime)
    }
}