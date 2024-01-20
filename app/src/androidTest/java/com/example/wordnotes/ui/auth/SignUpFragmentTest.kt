package com.example.wordnotes.ui.auth

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.ui.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpFragmentTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Before
    fun navigateToSignUpFragment() {
        onView(withId(R.id.text_sign_up)).perform(click())
    }

    @Test
    fun input_incorrectly_then_show_error_message() {
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_username)).perform(typeText("username"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_email)).perform(typeText("name@gmail.com"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_password)).perform(clearText())
        onView(withId(R.id.input_confirmed_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        onView(withId(R.id.input_password)).perform(typeText("123456"))
        onView(withId(R.id.input_email)).perform(typeText("name@gmail"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.the_email_address_isnt_in_the_correct_format)))
    }

    @Test
    fun press_back_should_return_to_sign_in_fragment() {
        pressBack()
        onView(withId(R.id.text_title)).check(matches(withText(R.string.sign_in)))
    }
}