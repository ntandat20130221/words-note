package com.example.wordnotes.ui.settings

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
import com.example.wordnotes.utils.setUpToolbar

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        val list = findPreference<ListPreference>("list_remind_times")
        list?.apply {
            entries = (1..100).map { "$it times" }.toList().toTypedArray()
            entryValues = (1..100).map { it.toString() }.toList().toTypedArray()
        }
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
        findNavController().setUpToolbar(
            view.findViewById<Toolbar>(R.id.toolbar).findViewById(R.id.toolbar),
            AppBarConfiguration(setOf(R.id.words_fragment, R.id.settings_fragment))
        )
    }
}