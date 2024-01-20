package com.example.wordnotes.ui

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperLogged
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.testutils.withCheckedItem
import com.example.wordnotes.testutils.withNavController
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @BindValue
    @JvmField
    val loggedFirebase: FirebaseAuthWrapper = TestFirebaseAuthWrapperLogged()

    @Test
    fun pressBackShouldExitApp() {
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }

    @Test
    fun testNavigationWithBottomNav() {
        // Navigate to LearningFragment
        onView(withId(R.id.learning_fragment)).perform(click())
        onView(withId(R.id.learning_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.learning_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.learning_fragment)))

        // Navigate to AccountFragment
        onView(withId(R.id.account_fragment)).perform(click())
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.account_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.account_fragment)))

        // Press Back to HomeFragment
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))

        // Navigate to AccountFragment
        onView(withId(R.id.account_fragment)).perform(click())
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.account_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.account_fragment)))

        // Simulate configuration change
        activityScenarioRule.scenario.recreate()

        // Press Back to HomeFragment
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))

        // Press Back again then exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }

    @Test
    fun multiClickBottomNavItemShouldNotChangeDestination() {
        // Double click on home BottomNavItem
        onView(withId(R.id.home_fragment)).perform(click())
        onView(withId(R.id.home_fragment)).perform(click())
        onView(withId(R.id.home_fragment)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))

        // Triple click on learning BottomNavItem
        onView(withId(R.id.learning_fragment)).perform(click())
        onView(withId(R.id.learning_fragment)).perform(click())
        onView(withId(R.id.learning_fragment)).perform(click())
        onView(withId(R.id.learning_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.learning_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.learning_fragment)))

        // Press Back to HomeFragment
        pressBack()
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.home_fragment)))
    }
}