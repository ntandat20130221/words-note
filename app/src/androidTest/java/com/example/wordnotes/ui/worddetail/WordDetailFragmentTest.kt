package com.example.wordnotes.ui.worddetail

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperLogged
import com.example.wordnotes.testutils.withNavController
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.home.WordsViewHolder
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class WordDetailFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @BindValue
    @JvmField
    val loggedFirebaseAuth: FirebaseAuthWrapper = TestFirebaseAuthWrapperLogged()

    @Inject
    lateinit var wordRepository: WordRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun openFragmentThenUiStateUpdateCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(doesNotExist())
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.word_detail_fragment) }
        onView(withId(R.id.text_word)).check(matches(withText("word1")))
        onView(withId(R.id.text_ipa)).check(matches(withText("ipa1")))
        onView(withId(R.id.text_pos)).check(matches(withText("noun")))
        onView(withId(R.id.text_meaning)).check(matches(withText("meaning1")))
        onView(withId(R.id.text_remind)).check(matches(withText(R.string.stop_remind)))

        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(doesNotExist())
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.word_detail_fragment) }
        onView(withId(R.id.text_word)).check(matches(withText("word1")))
        onView(withId(R.id.text_ipa)).check(matches(withText("ipa1")))
        onView(withId(R.id.text_pos)).check(matches(withText("noun")))
        onView(withId(R.id.text_meaning)).check(matches(withText("meaning1")))
        onView(withId(R.id.text_remind)).check(matches(withText(R.string.stop_remind)))
    }

    @Test
    fun updateRepositoryShouldUpdateUiState() = runTest {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        val updatedWord = Word(id = "1", word = "word_updated", pos = "noun", ipa = "ipa1", meaning = "meaning1", isRemind = false)
        wordRepository.updateWords(listOf(updatedWord))
        onView(withId(R.id.word_detail_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.word_detail_fragment) }
        onView(withId(R.id.text_word)).check(matches(withText("word_updated")))
        onView(withId(R.id.text_ipa)).check(matches(withText("ipa1")))
        onView(withId(R.id.text_pos)).check(matches(withText("noun")))
        onView(withId(R.id.text_meaning)).check(matches(withText("meaning1")))
        onView(withId(R.id.text_remind)).check(matches(withText(R.string.pref_title_remind)))
    }

    @Test
    fun clickEditActionShouldNavigateToAddEditWordFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.action_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.add_edit_word_fragment) }

        // Press back should return HomeFragment.
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
    }

    @Test
    fun clickDeleteActionShouldDismissAndUpdateRepository() = runTest {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.action_delete)).perform(click())
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(10)
    }

    @Test
    fun clickRemindActionShouldDismissAndUpdateRepository() = runTest {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.action_remind)).perform(click())
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        assertThat((wordRepository.getWords() as Result.Success).data.find { it.id == "1" }!!.isRemind).isFalse()
    }
}