package com.example.wordnotes.testutils

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.wordnotes.R
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class SignInRule : TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)

        onView(withId(R.id.input_email)).perform(replaceText("test@gmail.com"))
        onView(withId(R.id.input_password)).perform(replaceText("test123"))
        onView(withId(R.id.button_sign_in)).perform(click())
    }
}