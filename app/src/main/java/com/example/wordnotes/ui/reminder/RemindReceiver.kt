package com.example.wordnotes.ui.reminder

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.Configuration
import com.example.wordnotes.CHANNEL_ID
import com.example.wordnotes.R
import com.example.wordnotes.WordNotesApplication
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalTime

class RemindReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val appContainer = (context.applicationContext as WordNotesApplication).appContainer
        val wordReminder = appContainer.wordReminderFactory.create()

        try {
            if (isTimeOut(appContainer.reminderPreferencesFactory.create())) {
                wordReminder.schedule(next = true)
            } else {
                val jobInfo = JobInfo.Builder(0, ComponentName(context, RemindJobService::class.java)).build()
                val jobScheduler = context.getSystemService(JobScheduler::class.java)
                jobScheduler.schedule(jobInfo)
            }
        } catch (_: Exception) {
            wordReminder.cancel()
        }
    }

    private fun isTimeOut(reminderPreferences: ReminderPreferences): Boolean {
        val endTime = TimePickerPreference.Formatter.parse(reminderPreferences.getEndTime()!!)
        return LocalTime.now().isAfter(endTime)
    }
}

class RemindJobService : JobService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        Configuration.Builder().setJobSchedulerJobIdRange(0, Integer.MAX_VALUE).build()
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        val wordsRepository = (applicationContext as WordNotesApplication).appContainer.wordsRepository

        scope.launch {
            val result = wordsRepository.getRemindWords()
            if (result is Result.Success) {
                result.data.randomOrNull()?.let { word ->
                    pushNotification(word)
                }
            }
            jobFinished(params, false)
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        scope.cancel()
        return false
    }

    private fun pushNotification(word: Word) {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(applicationContext, CHANNEL_ID)
            .setSubText(getString(R.string.reminder))
            .setContentTitle("${word.word} ${word.ipa}")
            .setContentText("(${word.pos}) ${word.meaning}")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setShowWhen(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
        notificationManager.notify(word.id.hashCode(), notification)
    }
}