package com.example.wordnotes.ui.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.example.wordnotes.R
import com.example.wordnotes.WordNotesApplication
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.utils.setUpToolbar

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false).also {
            it.findViewById<FrameLayout>(R.id.settings_container).apply {
                addView(super.onCreateView(inflater, this, savedInstanceState))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar).findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.settings)
        findNavController().setUpToolbar(
            toolbar,
            AppBarConfiguration(setOf(R.id.words_fragment, R.id.settings_fragment))
        )
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as? MainActivity)?.setBottomNavigationVisibility(View.VISIBLE)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            KEY_REMIND, KEY_REMIND_TIMES, KEY_START_TIME, KEY_END_TIME -> {
                val wordReminder = (requireContext().applicationContext as WordNotesApplication).appContainer.wordReminderFactory.create()
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