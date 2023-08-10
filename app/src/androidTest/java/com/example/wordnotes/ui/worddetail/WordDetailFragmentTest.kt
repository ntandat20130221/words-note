package com.example.wordnotes.ui.worddetail

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.testutils.InitSomeWordItemsRule
import com.example.wordnotes.testutils.atPosition
import com.example.wordnotes.testutils.hasItemCount
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.words.WordsViewHolder
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test

class WordDetailFragmentTest {
    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    companion object {
        @get:ClassRule
        @JvmStatic
        val initSomeWordItemsRule = InitSomeWordItemsRule()
    }

    @Test
    fun testUiPopulatedCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))

        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        onView(allOf(withParent(withId(R.id.word_detail_fragment_layout)), withId(R.id.text_word))).check(matches(withText("word")))
        onView(allOf(withParent(withId(R.id.word_detail_fragment_layout)), withId(R.id.text_ipa))).check(matches(withText("ipa")))
        onView(allOf(withParent(withId(R.id.word_detail_fragment_layout)), withId(R.id.text_pos))).check(matches(withText("verb")))
        onView(allOf(withParent(withId(R.id.word_detail_fragment_layout)), withId(R.id.text_meaning))).check(matches(withText("meaning")))
        onView(withId(R.id.text_remind)).check(matches(withText(R.string.stop_remind)))
    }

    @Test
    fun deleteWord_FragmentDestroyed_WordsRecyclerViewUpdatedCorrectly() {
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(3))
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(allOf(isDescendantOfA(withId(R.id.word_detail_fragment_layout)), withId(R.id.action_delete))).perform(click())

        onView(withId(R.id.word_detail_fragment_layout)).check(doesNotExist())
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(2))
    }

    @Test
    fun editWord_FragmentDestroyed_NavigateToAddEditWordFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(allOf(isDescendantOfA(withId(R.id.word_detail_fragment_layout)), withId(R.id.action_edit))).perform(click())

        onView(withId(R.id.word_detail_fragment_layout)).check(doesNotExist())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun remindWord_FragmentDestroyed_WordsRecyclerViewUpdatedCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(allOf(isDescendantOfA(withId(R.id.word_detail_fragment_layout)), withId(R.id.action_remind))).perform(click())

        onView(withId(R.id.word_detail_fragment_layout)).check(doesNotExist())
        onView(withId(R.id.words_recycler_view))
            .check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.image_remind), not(isDisplayed()))))))
    }
}