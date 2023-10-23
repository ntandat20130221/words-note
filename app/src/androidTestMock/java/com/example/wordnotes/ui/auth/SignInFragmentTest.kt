package com.example.wordnotes.ui.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.ui.MainActivity
import org.junit.Rule
import org.junit.Test

class SignInFragmentTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun check_if_current_screen_is_sign_in_screen() {
        onView(withId(R.id.text_title)).check(matches(withText(R.string.sign_in)))
    }

    @Test
    fun input_incorrectly_then_show_error_message() {
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_email)).perform(typeText("name@gmail.com"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_email)).perform(clearText())
        onView(withId(R.id.input_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_email)).perform(typeText("name@gmail"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.the_email_address_isnt_in_the_correct_format)))
    }

    @Test(expected = NoActivityResumedException::class)
    fun press_back_should_finish_activity() {
        pressBack()
    }

    @Test(expected = NoActivityResumedException::class)
    fun test_navigating_to_sign_up_fragment() {
        onView(withId(R.id.text_sign_up)).perform(click())
        onView(withId(R.id.text_title)).check(matches(withText(R.string.sign_up)))

        pressBack()
        onView(withId(R.id.text_title)).check(matches(withText(R.string.sign_in)))

        onView(withId(R.id.text_sign_up)).perform(click())
        onView(withId(R.id.text_sign_in)).perform(click())
        pressBack()
    }
}