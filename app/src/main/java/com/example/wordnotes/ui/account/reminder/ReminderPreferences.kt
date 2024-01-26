package com.example.wordnotes.ui.account.reminder

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReminderPreferences @Inject constructor(@ApplicationContext context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isRemind(default: Boolean = false) = sharedPreferences.getBoolean(ReminderPreferenceFragment.KEY_REMIND, default)

    fun getRemindTimes(default: Int = 0): Int = sharedPreferences.getString(ReminderPreferenceFragment.KEY_REMIND_TIMES, default.toString())!!.toInt()

    fun getStartTime(default: String? = null) = sharedPreferences.getString(ReminderPreferenceFragment.KEY_START_TIME, default)

    fun getEndTime(default: String? = null) = sharedPreferences.getString(ReminderPreferenceFragment.KEY_END_TIME, default)
}