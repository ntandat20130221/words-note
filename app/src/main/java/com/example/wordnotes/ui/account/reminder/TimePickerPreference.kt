package com.example.wordnotes.ui.account.reminder

import android.content.Context
import android.content.res.TypedArray
import android.text.format.DateFormat
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import com.example.wordnotes.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TimePickerPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    @Inject
    lateinit var wordPreferences: ReminderPreferences

    private var fragmentManager: FragmentManager? = null
    private var initialValue: String? = null

    init {
        summaryProvider = SummaryProvider<TimePickerPreference> { getPersistedString(initialValue) }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        getPersistedString(defaultValue.toString()).let { savedValue ->
            initialValue = savedValue
            persistString(savedValue)
        }
    }

    override fun onClick() {
        if (fragmentManager == null) return

        val initialTime = Formatter.parse(getPersistedString(initialValue ?: DEFAULT_INITIAL_TIME))
        val timePicker = MaterialTimePicker.Builder()
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTimeFormat(if (DateFormat.is24HourFormat(context)) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_24H)
            .setHour(initialTime.hour)
            .setMinute(initialTime.minute)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            if (isValid(timePicker.hour, timePicker.minute)) {
                persistString(Formatter.format(LocalTime.of(timePicker.hour, timePicker.minute)))
                notifyChanged()
            } else {
                showMessage()
            }
        }

        fragmentManager?.let { timePicker.show(it, null) }
    }

    private fun isValid(hour: Int, minute: Int): Boolean {
        val time = LocalTime.of(hour, minute)

        return when (key) {
            ReminderFragment.KEY_START_TIME -> {
                val endTime = Formatter.parse(wordPreferences.getEndTime() ?: DEFAULT_END_TIME)
                time.isBefore(endTime) && (time.compareTo(endTime) != 0)
            }

            ReminderFragment.KEY_END_TIME -> {
                val startTime = Formatter.parse(wordPreferences.getStartTime() ?: DEFAULT_START_TIME)
                time.isAfter(startTime) && (time.compareTo(startTime) != 0)
            }

            else -> false
        }
    }

    fun setFragmentManager(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
    }

    private fun showMessage() {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.error)
            .setMessage(
                if (key == ReminderFragment.KEY_START_TIME) R.string.start_time_must_be_sooner_than_end_time
                else R.string.end_time_must_be_later_than_start_time
            )
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    class Formatter {
        companion object {
            private const val TIME_PREF_FORMAT = "HH:mm"

            fun parse(text: String): LocalTime = LocalTime.parse(text, DateTimeFormatter.ofPattern(TIME_PREF_FORMAT))

            fun format(localTime: LocalTime): String = localTime.format(DateTimeFormatter.ofPattern(TIME_PREF_FORMAT))
        }
    }

    companion object {
        private const val DEFAULT_INITIAL_TIME = "00:00"
        const val DEFAULT_START_TIME = "06:00"
        const val DEFAULT_END_TIME = "22:00"
    }
}