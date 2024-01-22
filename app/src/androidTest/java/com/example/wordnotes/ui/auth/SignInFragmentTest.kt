package com.example.wordnotes.ui.auth

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.mocks.TestFirebaseAuthWrapper
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.testutils.withNavController
import com.example.wordnotes.ui.MainActivity
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class SignInFragmentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class TestFirebaseModule {

        @Singleton
        @Binds
        abstract fun bind(testFirebaseAuthWrapper: TestFirebaseAuthWrapper): FirebaseAuthWrapper
    }

    @Test
    fun enterIncorrectInformationShouldShowError() {
        // Press sign in button without entering anything
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter email only
        onView(withId(R.id.input_email)).perform(typeText("name@gmail.com"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter password only
        onView(withId(R.id.input_email)).perform(clearText())
        onView(withId(R.id.input_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter email in incorrect format
        onView(withId(R.id.input_email)).perform(typeText("name@gmail"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.the_email_address_isnt_in_the_correct_format)))
    }

    @Test
    fun enterCorrectInformationShouldNavigateToHomeFragment() {
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }

        // Press back should exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }

    @Test
    fun signInWithNonExistingUserShouldShowError() {
        onView(withId(R.id.input_email)).perform(typeText("user4@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("4444444"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.authentication_failed)))
    }

    @Test
    fun navigatedFromSignUpFragmentShouldClearText() {
        // Enter email and password then navigate to SignUpFragment
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.text_sign_up)).perform(click())

        // Navigate back to SignInFragment then check inputs
        onView(withId(R.id.text_sign_in)).perform(click())
        onView(withId(R.id.input_email)).check(matches(withText("")))
        onView(withId(R.id.input_password)).check(matches(withText("")))

        // Continue entering email and password then navigate to SignUpFragment
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.text_sign_up)).perform(click())

        // Simulate configuration change then navigate back to SignInFragment then check inputs
        activityScenarioRule.scenario.recreate()
        onView(withId(R.id.text_sign_in)).perform(click())
        onView(withId(R.id.input_email)).check(matches(withText("")))
        onView(withId(R.id.input_password)).check(matches(withText("")))
    }

    @Test
    fun recreateActivityThenSignInShouldNavigateToHomeFragment() {
        // Recreate activity then sign in
        activityScenarioRule.scenario.recreate()
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(R.id.home_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }

        // Press back should exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }
}