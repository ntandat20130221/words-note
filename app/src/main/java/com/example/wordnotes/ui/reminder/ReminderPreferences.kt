package com.example.wordnotes.ui.reminder

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class ReminderPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isRemind(default: Boolean = false) = sharedPreferences.getBoolean(ReminderFragment.KEY_REMIND, default)

    fun getRemindTimes(default: Int = 0): Int = sharedPreferences.getString(ReminderFragment.KEY_REMIND_TIMES, default.toString())!!.toInt()

    fun getStartTime(default: String? = null) = sharedPreferences.getString(ReminderFragment.KEY_START_TIME, default)

    fun getEndTime(default: String? = null) = sharedPreferences.getString(ReminderFragment.KEY_END_TIME, default)
}