package com.example.wordnotes.ui.words

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openContextualActionModeOverflowMenu
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.testutils.AddSomeWordItemsRule
import com.example.wordnotes.testutils.atPosition
import com.example.wordnotes.testutils.hasItemCount
import com.example.wordnotes.testutils.withBackgroundColor
import com.example.wordnotes.ui.MainActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test

class WordsFragmentTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val addSomeWordItemsRule = AddSomeWordItemsRule()

    @Test
    fun startThenStopActionMode_FabAndBottomNavDisplayCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, longClick()))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))

        onView(withId(com.google.android.material.R.id.action_mode_close_button)).perform(click())
        onView(withId(R.id.fab_add_word)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun startActionMode_SelectSomeItems_Recreate_PersistUiState() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))

        onView(withId(com.google.android.material.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))

        activityScenarioRule.scenario.recreate()

        onView(withId(com.google.android.material.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.fab_add_word)).check(matches(not(isDisplayed())))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
    }

    @Test
    fun openActionMode_SelectSomeItems_EditMenuItemDisplayProperly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.menu_edit)).check(ViewAssertions.doesNotExist())

        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.menu_edit)).check(matches(isDisplayed()))
    }

    @Test
    fun openActionMode_SelectAnyItem_ClickEditMenu_ShouldNavigateToAddEditWordFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))

        onView(withId(R.id.menu_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun openActionMode_SelectSomeItems_ClickDeleteMenu_WordsRecyclerViewUpdateCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))

        onView(withId(R.id.menu_delete)).perform(click())
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(1))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("word2")))))
    }

    @Test
    fun openActionMode_SelectSomeItems_ClickRemindMenu_WordsRecyclerViewUpdateCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(2, click()))

        onView(withId(R.id.menu_remind)).perform(click())
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.image_remind), isDisplayed())))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, hasDescendant(allOf(withId(R.id.image_remind), isDisplayed())))))
    }

    @Test
    fun openActionMode_ClickSelectAllMenu_WordsRecyclerViewUpdateCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))

        openContextualActionModeOverflowMenu()
        onView(withText(R.string.select_all)).perform(click())

        onView(withId(R.id.words_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(2, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun searchingWithKeyword_CountRecyclerViewItems() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.search_view_root)).check(matches(isDisplayed()))

        onView(withId(com.example.customviews.R.id.input_search)).perform(typeText("w"))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(3))

        onView(withId(com.example.customviews.R.id.input_search)).perform(clearText(), typeText("word2"))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(1))
    }

    @Test
    fun searchWithKeyword_Recreate_PersistUiState() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(typeText("word2"))

        activityScenarioRule.scenario.recreate()

        onView(withId(com.example.customviews.R.id.search_view_root)).check(matches(isDisplayed()))
        onView(withId(com.example.customviews.R.id.input_search)).check(matches(withText("word2")))
        onView(withId(R.id.search_recycler_view)).check(hasItemCount(1))
    }

    @Test
    fun startSearchThenStartActionMode_WordsRecyclerViewUpdateCorrectly() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(typeText("w"))

        onView(withId(R.id.search_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(com.google.android.material.R.id.action_bar_title)).check(matches(withText("1")))

        onView(withId(R.id.search_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))
        onView(withId(com.google.android.material.R.id.action_bar_title)).check(matches(withText("2")))

        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
    }

    @Test
    fun test() {
        onView(withId(R.id.menu_search)).perform(click())
        onView(withId(com.example.customviews.R.id.input_search)).perform(typeText("w"))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, longClick()))
        onView(withId(R.id.search_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(1, click()))

        activityScenarioRule.scenario.recreate()

        onView(withId(com.google.android.material.R.id.action_bar_title)).check(matches(withText("2")))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, withBackgroundColor(R.attr.color_selected_item_background))))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(1, withBackgroundColor(R.attr.color_selected_item_background))))
    }
}