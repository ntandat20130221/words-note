package com.example.wordnotes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.words.WordsViewHolder
import com.example.wordnotes.utils.atPosition
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.StringContains.containsString
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun longClickItem_OpenActionMode_SelectMoreItems_RemainRecreate() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))

        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.menu_select_all)).check(matches(isDisplayed()))
        onView(withText(containsString("2 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.words_recycler_view))
            .check(matches(atPosition(0, hasDescendant(withText("words")))))

        activityScenarioRule.scenario.recreate()

        onView(withId(R.id.menu_select_all)).check(matches(isDisplayed()))
        onView(withText(containsString("2 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
    }

    @Test
    fun openActionMode_ClickMoreItemsUntilCloseActionMode() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))

        onView(withId(R.id.menu_select_all)).check(matches(isDisplayed()))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))

        onView(withId(R.id.menu_select_all)).check(doesNotExist())
    }

    @Test
    fun openActionMode_SelectMoreItems_CheckEditItemDisplayProperly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.menu_edit)).check(doesNotExist())
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
    }
}