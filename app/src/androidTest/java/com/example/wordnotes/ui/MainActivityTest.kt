package com.example.wordnotes.ui

import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun multiClickSettingBottomNavItem_DestinationDidNotChange() {
        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment_layout)).check(matches(isDisplayed()))
        withNavController { assertThat(it.currentDestination?.id).isEqualTo(R.id.settings_fragment) }

        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment)).perform(click())
        withNavController { assertThat(it.currentDestination?.id).isEqualTo(R.id.settings_fragment) }
    }

    @Test
    fun multiClickSettingBottomNavItem_PressBack_ReturnWordsFragment() {
        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment)).perform(click())
        pressBack()
        onView(withId(R.id.words_fragment)).check(matches(isDisplayed()))
        withNavController { assertThat(it.currentDestination?.id).isEqualTo(R.id.words_fragment) }
    }

    @Test
    fun navigateAroundApp_BottomBarDisplayCorrectly() {
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
        pressBack()
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        onView(withId(R.id.words_fragment)).perform(click())
        onView(withId(R.id.bottom_nav)).check(matches(isDisplayed()))
        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.bottom_nav)).check(matches(not(isDisplayed())))
    }

    private fun withNavController(onNavController: (NavController) -> Unit) {
        activityScenarioRule.scenario.onActivity { mainActivity ->
            onNavController(mainActivity.navController)
        }
    }
}