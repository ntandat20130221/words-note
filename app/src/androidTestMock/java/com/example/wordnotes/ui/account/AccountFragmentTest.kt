package com.example.wordnotes.ui.account

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.testutils.SignInRule
import com.example.wordnotes.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class AccountFragmentTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val signInRule = SignInRule()

    @Test(expected = NoActivityResumedException::class)
    fun test() {
        onView(withId(R.id.account_fragment)).perform(click())
        onView(withId(R.id.account_fragment)).check(matches(isDisplayed()))
        pressBack()
        pressBack()
    }
}