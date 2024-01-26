package com.example.wordnotes.ui.account.reminder

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.example.wordnotes.R
import com.example.wordnotes.utils.setUpToolbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_reminder, container, false).also {
            childFragmentManager.beginTransaction()
                .replace(R.id.container, ReminderPreferenceFragment())
                .commit()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar).findViewById<Toolbar>(R.id.toolbar)
        findNavController().setUpToolbar(toolbar)
    }
}

@AndroidEntryPoint
class ReminderPreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    @Inject
    lateinit var wordReminder: WordReminder

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.reminder_preference, rootKey)
        preparePreferences()
    }

    private fun preparePreferences() {
        findPreference<ListPreference>(KEY_REMIND_TIMES)?.apply {
            entries = (1..100).map { context.resources.getQuantityString(R.plurals.remind_times, it, it) }.toList().toTypedArray()
            entryValues = (1..100).map { it.toString() }.toList().toTypedArray()
        }
        findPreference<TimePickerPreference>(KEY_START_TIME)?.setFragmentManager(childFragmentManager)
        findPreference<TimePickerPreference>(KEY_END_TIME)?.setFragmentManager(childFragmentManager)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            KEY_REMIND, KEY_REMIND_TIMES, KEY_START_TIME, KEY_END_TIME -> {
                if (sharedPreferences.getBoolean(KEY_REMIND, false))
                    wordReminder.schedule()
                else
                    wordReminder.cancel()
            }
        }
    }

    companion object {
        const val KEY_REMIND = "pref_remind"
        const val KEY_REMIND_TIMES = "pref_remind_times"
        const val KEY_START_TIME = "pref_start_time"
        const val KEY_END_TIME = "pref_end_time"
    }
}