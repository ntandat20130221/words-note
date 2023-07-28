package com.example.wordnotes.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class WordReminder(
    private val context: Context,
    private val wordPreferences: WordPreferences
) {
    private val alarmManager = context.applicationContext.getSystemService(AlarmManager::class.java)

    fun schedule(next: Boolean = false) {
        cancel()
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerTime(next = next), getInterval(), getPendingIntent())
    }

    fun cancel() {
        val cancelPendingIntent = getPendingIntent(isCancel = true)
        cancelPendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun getTriggerTime(valueIfError: Long = System.currentTimeMillis(), next: Boolean = false): Long {
        return try {
            val startTime = TimePickerPreference.Formatter.parse(wordPreferences.getStartTime()!!)
            SystemClock.elapsedRealtime()
                .plus(ChronoUnit.MILLIS.between(LocalTime.now(), startTime))
                .plus(if (next) TimeUnit.DAYS.toMillis(1) else 0)
        } catch (_: Exception) {
            valueIfError
        }
    }

    private fun getInterval(valueIfError: Long = AlarmManager.INTERVAL_HOUR): Long {
        return try {
            val startTime = TimePickerPreference.Formatter.parse(wordPreferences.getStartTime()!!)
            val endTime = TimePickerPreference.Formatter.parse(wordPreferences.getEndTime()!!)
            val remindTimes = wordPreferences.getRemindTimes()

            val interval = ChronoUnit.MILLIS.between(startTime, endTime) / remindTimes
            if (interval < 0) throw IllegalStateException() else interval
        } catch (_: Exception) {
            valueIfError
        }
    }

    private fun getPendingIntent(isCancel: Boolean = false): PendingIntent? {
        val broadcastIntent = Intent(context, RemindReceiver::class.java)
        val flags = if (isCancel) (PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context.applicationContext, 0, broadcastIntent, flags)
    }
}