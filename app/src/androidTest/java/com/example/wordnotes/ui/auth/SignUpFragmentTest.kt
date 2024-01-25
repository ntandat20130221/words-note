package com.example.wordnotes.ui.auth

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBackUnconditionally
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapper
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class SignUpFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityScenarioRule = activityScenarioRule<MainActivity>()

    @InstallIn(SingletonComponent::class)
    @Module
    abstract class TestFirebaseModule {

        @Singleton
        @Binds
        abstract fun bind(testFirebaseAuthWrapper: TestFirebaseAuthWrapper): FirebaseAuthWrapper
    }

    @Before
    fun navigateToSignUpFragment() {
        onView(withId(R.id.text_sign_up)).perform(click())
    }

    @Test
    fun enterIncorrectInformationShouldShowError() {
        // Press sign up button without entering anything
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter username only
        onView(withId(R.id.input_username)).perform(typeText("name"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter username and email
        onView(withId(R.id.input_email)).perform(typeText("name@gmail.com"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter username, email and password
        onView(withId(R.id.input_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.please_complete_all_information)))

        // Enter username, email, password and confirm password but email in invalid format
        onView(withId(R.id.input_email)).perform(replaceText("name@gmail"))
        onView(withId(R.id.input_confirmed_password)).perform(typeText("123456"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.the_email_address_isnt_in_the_correct_format)))

        // Enter valid email but password length less than 6
        onView(withId(R.id.input_email)).perform(replaceText("name@gmail.com"))
        onView(withId(R.id.input_password)).perform(replaceText("12345"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.password_must_be_at_least_6_characters)))

        // Enter password and confirm password not match
        onView(withId(R.id.input_password)).perform(replaceText("111111"))
        onView(withId(R.id.input_password)).perform(replaceText("222222"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.confirm_password_didnt_match)))
    }

    @Test
    fun enterCorrectInformationThenPressSignUpShouldNavigateToSignInFragment() {
        onView(withId(R.id.input_username)).perform(typeText("user4"))
        onView(withId(R.id.input_email)).perform(typeText("user4@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("444444"))
        onView(withId(R.id.input_confirmed_password)).perform(typeText("444444"))
        onView(withId(R.id.button_sign_up)).perform(click())
        onView(withId(R.id.sign_in_fragment_layout)).check(matches(ViewMatchers.isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_in_fragment) }

        // Press back should exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }

    @Test
    fun signInWithNewlySignedUpUserShouldNavigateToHomeFragment() {
        // Sign up
        onView(withId(R.id.input_username)).perform(typeText("user4"))
        onView(withId(R.id.input_email)).perform(typeText("user4@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("444444"))
        onView(withId(R.id.input_confirmed_password)).perform(typeText("444444"))
        onView(withId(R.id.button_sign_up)).perform(click())

        // Sign in
        onView(withId(R.id.input_email)).perform(typeText("user4@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("444444"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(R.id.home_fragment_layout)).check(matches(ViewMatchers.isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.home_fragment) }

        // Press back should exit app
        pressBackUnconditionally()
        assertThat(activityScenarioRule.scenario.state.isAtLeast(Lifecycle.State.DESTROYED)).isTrue()
    }
}