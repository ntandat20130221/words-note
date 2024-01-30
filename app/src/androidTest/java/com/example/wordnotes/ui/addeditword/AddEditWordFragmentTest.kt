package com.example.wordnotes.ui.addeditword

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.repositories.WordRepository
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperLogged
import com.example.wordnotes.testutils.atPosition
import com.example.wordnotes.testutils.isKeyboardOpened
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
class AddEditWordFragmentTest {

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
    fun startAddingWordThenUiStateShouldUpdateCorrectly() {
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.add_edit_word_fragment) }
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.add_new_word))))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        assertThat(isKeyboardOpened()).isTrue()
        onView(withId(R.id.input_word)).check(matches(withText("")))
        onView(withId(R.id.input_ipa)).check(matches(withText("")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(0, isSelected())))
        onView(withId(R.id.input_meaning)).check(matches(withText("")))
        onView(withId(R.id.check_remind)).check(matches(isChecked()))

        // Press back should return HomeFragment.
        closeSoftKeyboard()
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.words))))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun startEditingWordThenUiStateShouldUpdateCorrectly() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(0, click()))
        onView(withId(R.id.action_edit)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.add_edit_word_fragment) }
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.edit_word))))
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        assertThat(isKeyboardOpened()).isFalse()
        onView(withId(R.id.input_word)).check(matches(withText("word1")))
        onView(withId(R.id.input_ipa)).check(matches(withText("ipa1")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(1, isSelected())))
        onView(withId(R.id.input_meaning)).check(matches(withText("meaning1")))
        onView(withId(R.id.check_remind)).check(matches(isChecked()))

        // Press back should return HomeFragment.
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.toolbar)).check(matches(hasDescendant(withText(R.string.words))))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
    }

    @Test
    fun startAddingWordThenRotateTheScreenShouldPersistUiState() {
        // Add new word.
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.input_word)).perform(replaceText("new word"))
        onView(withId(R.id.pos_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(1, click()))
        onView(withId(R.id.check_remind)).perform(scrollTo(), click())

        // Rotate the screen should persist ui state.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(R.id.input_word)).check(matches(withText("new word")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(1, isSelected())))
        onView(withId(R.id.check_remind)).check(matches(isNotChecked()))
    }

    @Test
    fun startEditingWordThenRotateTheScreenShouldPersistUiState() {
        // Edit word.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(2, click()))
        onView(withId(R.id.action_edit)).perform(click())
        onView(withId(R.id.input_word)).perform(replaceText("updated word"))
        onView(withId(R.id.pos_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(1, click()))
        onView(withId(R.id.check_remind)).perform(scrollTo(), click())

        // Rotate the screen should persist ui state.
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.setOrientationLeft()
        onView(withId(R.id.input_word)).check(matches(withText("updated word")))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(1, isSelected())))
        onView(withId(R.id.check_remind)).check(matches(isChecked()))
    }

    @Test
    fun startAddingWordThenSaveShouldReturnHomeFragmentAndUpdateRepository() = runTest {
        // Add new word then save.
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.input_word)).perform(typeText("new word"))
        onView(withId(R.id.pos_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(1, click()))
        onView(withId(R.id.check_remind)).perform(scrollTo(), click())
        onView(withId(R.id.menu_save)).perform(click())

        // After saving should return HomeFragment and repository updated.
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(12)
    }

    @Test
    fun startEditingWordThenSaveShouldReturnHomeFragmentAndUpdateRepository() = runTest {
        // Edit word then save.
        onView(withId(R.id.words_recycler_view)).perform(actionOnItem<WordsViewHolder>(hasDescendant(withText("word1")), click()))
        onView(withId(R.id.action_edit)).perform(click())
        onView(withId(R.id.input_word)).perform(replaceText("updated word"))
        onView(withId(R.id.pos_recycler_view)).perform(actionOnItemAtPosition<PartsOfSpeechViewHolder>(2, click()))
        onView(withId(R.id.check_remind)).perform(scrollTo(), click())
        onView(withId(R.id.menu_save)).perform(click())

        // After saving should return HomeFragment and repository updated.
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        assertThat((wordRepository.getWords() as Result.Success).data).hasSize(11)
        val updatedWord = (wordRepository.getWords() as Result.Success).data.find { it.id == "1" }!!
        assertThat(updatedWord.word).isEqualTo("updated word")
        assertThat(updatedWord.pos).isEqualTo("adj.")
        assertThat(updatedWord.isRemind).isFalse()
    }
}