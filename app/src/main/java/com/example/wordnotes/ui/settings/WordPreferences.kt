package com.example.wordnotes.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class WordPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isRemind(default: Boolean = false) = sharedPreferences.getBoolean(SettingsFragment.KEY_REMIND, default)

    fun getRemindTimes(default: Int = 0): Int = sharedPreferences.getString(SettingsFragment.KEY_REMIND_TIMES, default.toString())!!.toInt()

    fun getStartTime(default: String? = null) = sharedPreferences.getString(SettingsFragment.KEY_START_TIME, default)

    fun getEndTime(default: String? = null) = sharedPreferences.getString(SettingsFragment.KEY_END_TIME, default)
}