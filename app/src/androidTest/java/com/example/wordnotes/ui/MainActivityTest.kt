package com.example.wordnotes.ui

import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.atPosition
import com.example.wordnotes.ui.words.WordsViewHolder
import com.example.wordnotes.withBackgroundColor
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.StringContains.containsString
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

/**
 * This test class contains ActionMode related tests which only available in AppCompatActivity.
 */
class MainActivityTest {
    // TODO: Fix bug testing navigation
    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    companion object {
        @JvmStatic
        @BeforeClass
        fun setUp_AddSomeWords() {
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

            onView(withId(R.id.fab_add_word)).perform(click())
            onView(withId(R.id.input_word)).perform(click(), typeText("word4"))
            onView(withId(R.id.menu_save)).perform(click())
        }
    }

    @Test
    fun openThenCloseActionMode_FabAndBottomNavDisplayCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withText(containsString("1 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withText(containsString("2 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withText(containsString("1 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withText(containsString("1 selected"))).check(doesNotExist())
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withText(containsString("1 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(com.google.android.material.R.id.action_mode_close_button)).perform(click())
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun longClickItemOpenActionMode_SelectMoreItems_PersistUiStateAcrossRecreate() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))

        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))

        activityScenarioRule.scenario.recreate()

        onView(withText(containsString("2 selected"))).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun openActionMode_SelectMoreItems_EditItemDisplayProperly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.menu_edit)).check(doesNotExist())

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
    }
}