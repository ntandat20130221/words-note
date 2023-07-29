package com.example.wordnotes.ui.settings

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wordnotes.WordNotesApplication

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val wordReminder = (context.applicationContext as WordNotesApplication).appContainer.wordReminderFactory.create()
            wordReminder.schedule()
        }
    }
}