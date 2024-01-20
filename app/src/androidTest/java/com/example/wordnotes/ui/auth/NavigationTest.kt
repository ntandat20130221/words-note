package com.example.wordnotes.ui.auth

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
import com.example.wordnotes.mocks.TestFirebaseAuthWrapperNotLogged
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.testutils.withNavController
import com.example.wordnotes.ui.MainActivity
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Rule
import org.junit.Test

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @BindValue
    @JvmField
    val firebaseAuth: FirebaseAuthWrapper = TestFirebaseAuthWrapperNotLogged()

    @Test
    fun test() {
        // Navigate to SignUpFragment
        onView(withId(R.id.text_sign_up)).perform(click())
        onView(withId(R.id.sign_up_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_up_fragment) }

        // Press back to SignInFragment
        pressBack()
        onView(withId(R.id.sign_in_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_in_fragment) }

        // Navigate to ForgotPasswordFragment
        onView(withId(R.id.text_forgot_password)).perform(click())
        onView(withId(R.id.forgot_password_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.forgot_password_fragment) }

        // Press back to SignInFragment
        pressBack()
        onView(withId(R.id.sign_in_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_in_fragment) }

        // Navigate to SignUpFragment
        onView(withId(R.id.text_sign_up)).perform(click())
        onView(withId(R.id.sign_up_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_up_fragment) }

        // Simulate configuration change
        activityScenarioRule.scenario.recreate()
        onView(withId(R.id.sign_up_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_up_fragment) }

        // Navigate to SignInFragment
        onView(withId(R.id.text_sign_in)).perform(click())
        onView(withId(R.id.sign_in_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_in_fragment) }

        // Press back should exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }
}