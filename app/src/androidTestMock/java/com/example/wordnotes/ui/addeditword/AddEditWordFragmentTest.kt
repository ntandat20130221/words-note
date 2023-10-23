package com.example.wordnotes.ui.addeditword

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isFocused
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotSelected
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.wordnotes.R
import com.example.wordnotes.testutils.SignInRule
import com.example.wordnotes.testutils.atPosition
import com.example.wordnotes.testutils.hasItemCount
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.words.WordsViewHolder
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test

class AddEditWordFragmentTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @get:Rule
    val signInRule = SignInRule()

    @Test
    fun addNewWord_AtInitial_InputWordGetFocus_SoftKeyboardShown() {
        addNewWord()
        onView(withId(R.id.input_word)).check(matches(isFocused()))
        assertThat(isKeyboardOpenedShellCheck()).isTrue()
    }

    @Test
    fun addNewWord_AtInitial_PosRecycler_ShouldAtVerbItem() {
        addNewWord()
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(0, isSelected())))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(1, isNotSelected())))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(2, isNotSelected())))
    }

    @Test
    fun addNewWord_FillSomeInputs_Recreate_PersistUiState() {
        addNewWord()
        onView(withId(R.id.input_word)).perform(click(), typeText("word"))
        onView(withId(R.id.pos_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(2, click()))
        onView(withId(R.id.check_remind)).perform(click())

        activityScenarioRule.scenario.recreate()

        onView(withId(R.id.input_word)).check(matches(withText("word")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(2, isSelected())))
        onView(withId(R.id.check_remind)).check(matches(isChecked()))
    }

    @Test
    fun addNewWord_SaveWord_SnackBarShownWithSuccessfulText_RecyclerViewUpdatedCorrectly() {
        addNewWord()
        onView(withId(R.id.input_word)).perform(click(), typeText("new word"))
        onView(withId(R.id.menu_save)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.add_new_word_successfully)))
        onView(withId(R.id.words_recycler_view)).check(hasItemCount(4))
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(3, hasDescendant(withText("new word")))))
    }

    @Test
    fun addNewWord_LeftWordBlank_ClickSaveMenu_SnackBarShownWithWarningText() {
        addNewWord()
        onView(withId(R.id.menu_save)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.word_must_not_be_empty)))
    }

    @Test
    fun editWord_UiPopulatedCorrectly() {
        editWord()
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.input_word)).check(matches(withText("word")))
        onView(withId(R.id.input_ipa)).check(matches(withText("ipa")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(0, isSelected())))
        onView(withId(R.id.input_meaning)).check(matches(withText("meaning")))
        onView(withId(R.id.check_remind)).check(matches(isNotChecked()))
    }

    @Test
    fun editWord_UpdateWord_SaveWord_WordsRecyclerViewUpdatedCorrectly() {
        editWord()
        onView(withId(R.id.input_word)).perform(click(), replaceText("word updated"))
        onView(withId(R.id.check_remind)).perform(click())
        onView(withId(R.id.menu_save)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(doesNotExist())
        onView(withId(R.id.words_recycler_view)).check(matches(atPosition(0, hasDescendant(withText("word updated")))))
        onView(withId(R.id.words_recycler_view))
            .check(matches(atPosition(0, hasDescendant(allOf(withId(R.id.image_remind), isDisplayed())))))
    }

    private fun editWord() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        onView(withId(R.id.action_edit)).perform(click())
    }

    private fun addNewWord() {
        onView(withId(R.id.fab_add_word)).perform(click())
    }

    private fun isKeyboardOpenedShellCheck(): Boolean {
        val checkKeyboardCmd = "dumpsys input_method | grep mInputShown"
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .executeShellCommand(checkKeyboardCmd).contains("mInputShown=true")
    }
}