package com.example.wordnotes

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.words.WordsFragment
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun navigateToAddEditWordFragment_ClickHomeItem_OpenHomeFragment() {
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.words_fragment)).perform(click())
        onView(withId(R.id.words_fragment_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToAddEditWordFragment_ClickSettingsItem_PressBack_ReturnHomeFragment() {
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment_layout)).check(matches(isDisplayed()))

        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.words_fragment_layout)).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToAddEditWordFragment_ClickSettingsItem_ClickHomeItem_ReturnAddEditWordFragmentAndSaveStates_PressBack_ExitApp() {
        onView(withId(R.id.fab_add_word)).perform(click())

        onView(withId(R.id.input_words)).perform(replaceText("word"))
        onView(withId(R.id.check_learning)).perform(click())

        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.words_fragment)).perform(click())

        onView(withId(R.id.add_edit_word_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.input_words)).check(matches(withText("word")))
        onView(withId(R.id.check_learning)).check(matches(isChecked()))

        onView(isRoot()).perform(pressBack())
        onView(withId(R.id.words_fragment_layout)).check(matches(isDisplayed()))

        onView(isRoot()).perform(pressBackUnconditionally())
        assertThat(activityScenarioRule.scenario.state).isEqualTo(Lifecycle.State.DESTROYED)
    }

    @Test
    fun testNavigationComponents() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        launchFragmentInContainer(themeResId = R.style.Theme_WordNotes) {
            WordsFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }

        onView(withId(R.id.fab_add_word)).perform(click())
        assertEquals(navController.currentDestination?.id, R.id.add_edit_word_fragment)
    }
}