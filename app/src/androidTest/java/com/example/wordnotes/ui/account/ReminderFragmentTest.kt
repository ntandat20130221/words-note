package com.example.wordnotes.ui.account

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.wordnotes.R
import com.example.wordnotes.testutils.getQuantityString
import com.example.wordnotes.testutils.getString
import com.example.wordnotes.ui.account.reminder.ReminderFragment
import com.example.wordnotes.ui.account.reminder.ReminderPreferences
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ReminderFragmentTest {
    private lateinit var fragmentScenario: FragmentScenario<ReminderFragment>

    @Inject
    lateinit var wordPreferences: ReminderPreferences

    @Before
    fun setUp() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_WordNotes) {
            ReminderFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun checkPreferencesAndSharedPreferences_InSync() {
        // Is remind
        getRemindCheckBox().check(matches(if (wordPreferences.isRemind()) isChecked() else isNotChecked()))

        // Number of reminds a day
        getSummary(getString(R.string.pref_title_number_of_reminds_a_day))
            .check(
                matches(
                    withText(
                        getQuantityString(
                            R.plurals.remind_times, wordPreferences.getRemindTimes(), wordPreferences.getRemindTimes()
                        )
                    )
                )
            )

        // Start time
        getSummary(getString(R.string.pref_title_start_time)).check(matches(withText(wordPreferences.getStartTime())))

        // End time
        getSummary(getString(R.string.pref_title_end_time)).check(matches(withText(wordPreferences.getEndTime())))
    }

    private fun getRemindCheckBox() = onView(
        allOf(
            isDescendantOfA(
                hasDescendant(
                    allOf(
                        withId(android.R.id.title),
                        withText(getString(R.string.pref_title_remind))
                    )
                )
            ),
            withId(android.R.id.checkbox)
        )
    )

    private fun getSummary(title: String) = onView(
        allOf(
            withId(android.R.id.summary),
            hasSibling(allOf(withId(android.R.id.title), withText(title)))
        )
    )
}