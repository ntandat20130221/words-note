package com.example.wordnotes.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, getTriggerTime(next = next), getInterval(), getPendingIntent()!!)
        enableBootReceiver()
    }

    fun cancel() {
        getPendingIntent(forCanceling = true)?.let { alarmManager.cancel(it) }
        disableBootReceiver()
    }

    private fun enableBootReceiver() {
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)
    }

    private fun disableBootReceiver() {
        val receiver = ComponentName(context, BootReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
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

    private fun getPendingIntent(forCanceling: Boolean = false): PendingIntent? {
        val intent = Intent(context, RemindReceiver::class.java)
        val flags = if (forCanceling) PendingIntent.FLAG_NO_CREATE else PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context.applicationContext, 0, intent, flags)
    }
}