package com.example.wordnotes.testutils

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.wordnotes.R
import com.example.wordnotes.ui.MainActivity
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class InitSomeWordItemsRule : TestWatcher() {
    override fun starting(description: Description) {
        super.starting(description)
        launchActivity<MainActivity>()
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.input_word)).perform(click(), typeText("word1"))
        onView(withId(R.id.menu_save)).perform(click())

        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.input_word)).perform(click(), typeText("word2"))
        onView(withId(R.id.menu_save)).perform(click())

        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.input_word)).perform(click(), typeText("word3"))
        onView(withId(R.id.menu_save)).perform(click())
    }
}