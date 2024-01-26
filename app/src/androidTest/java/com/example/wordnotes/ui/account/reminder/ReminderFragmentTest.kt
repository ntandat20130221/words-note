package com.example.wordnotes.ui.account.reminder

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperLogged
import com.example.wordnotes.testutils.getQuantityString
import com.example.wordnotes.testutils.getString
import com.example.wordnotes.ui.MainActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class ReminderFragmentTest {

    @Inject
    lateinit var wordPreferences: ReminderPreferences

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @BindValue
    @JvmField
    val loggedFirebaseAuth: FirebaseAuthWrapper = TestFirebaseAuthWrapperLogged()

    @Before
    fun injectAndNavigateToReminderFragment() {
        hiltRule.inject()
        onView(withId(R.id.account_fragment)).perform(click())
        onView(withId(R.id.layout_reminder)).perform(click())
    }

    @Test
    fun testDefaultValueInPreferenceScreenShouldSyncWithSharePreferences() {
        // Remind
        val isRemind = wordPreferences.isRemind()
        onView(allOf(isDescendantOfA(hasSibling(hasDescendant(withText(getString(R.string.pref_title_remind))))), withId(android.R.id.checkbox)))
            .check(matches(if (isRemind) isChecked() else isNotChecked()))

        // Number of reminds a day
        val remindTimes = wordPreferences.getRemindTimes()
        onView(hasSibling(withText(getString(R.string.pref_title_number_of_reminds_a_day))))
            .check(matches(withText(getQuantityString(R.plurals.remind_times, remindTimes, remindTimes))))

        // Start time
        val startTime = wordPreferences.getStartTime()
        onView(hasSibling(withText(getString(R.string.pref_title_start_time))))
            .check(matches(withText(startTime)))

        // End time
        val endTime = wordPreferences.getEndTime()
        onView(hasSibling(withText(getString(R.string.pref_title_end_time))))
            .check(matches(withText(endTime)))
    }

    @Test
    fun changePreferencesThenCheckSharedPreferencesShouldInSync() {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Change Remind preference
        val checkBoxPreference =
            onView(allOf(isDescendantOfA(hasSibling(hasDescendant(withText(getString(R.string.pref_title_remind))))), withId(android.R.id.checkbox)))
        checkBoxPreference.perform(click())

        // Change Number of reminds a day preference
        val numberOfRemindsPreference = onView(hasSibling(withText(getString(R.string.pref_title_number_of_reminds_a_day))))
        numberOfRemindsPreference.perform(click())
        onData(allOf(instanceOf(String::class.java), `is`(getQuantityString(R.plurals.remind_times, 30, 30)))).perform(click())

        // Change Start time preference
        val startTimePreference = onView(hasSibling(withText(getString(R.string.pref_title_start_time))))
        startTimePreference.perform(click())
        uiDevice.findObject(By.desc("5 hours").text("5")).click()
        uiDevice.findObject(By.res("com.example.wordnotes:id/material_timepicker_ok_button")).click()
        uiDevice.wait(Until.gone(By.res("com.example.wordnotes:id/header_title").text("Select time")), 1000)

        // Change End time preference
        val endTimePreference = onView(hasSibling(withText(getString(R.string.pref_title_start_time))))
        endTimePreference.perform(click())
        uiDevice.findObject(By.desc("9 hours").text("9")).click()
        uiDevice.findObject(By.res("com.example.wordnotes:id/material_timepicker_ok_button")).click()
        uiDevice.wait(Until.gone(By.res("com.example.wordnotes:id/header_title").text("Select time")), 1000)

        // Change whether SharedPreferences is updated properly
        val isRemind = wordPreferences.isRemind()
        checkBoxPreference.check(matches(if (isRemind) isChecked() else isNotChecked()))
        val remindTimes = wordPreferences.getRemindTimes()
        numberOfRemindsPreference.check(matches(withText(getQuantityString(R.plurals.remind_times, remindTimes, remindTimes))))
        val startTime = wordPreferences.getStartTime()
        startTimePreference.check(matches(withText(startTime)))
        val endTime = wordPreferences.getStartTime()
        endTimePreference.check(matches(withText(endTime)))
    }
}