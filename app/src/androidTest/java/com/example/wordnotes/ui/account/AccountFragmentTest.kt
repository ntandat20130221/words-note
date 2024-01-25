package com.example.wordnotes.ui.account

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapper
import com.example.wordnotes.testutils.withCheckedItem
import com.example.wordnotes.testutils.withDrawable
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class AccountFragmentTest {

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
    fun signInAndNavigateToAccountFragment() = runTest {
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(R.id.account_fragment)).perform(click())
    }

    @Test
    fun checkIfCurrentScreenIsAccountFragment() {
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.account_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.account_fragment) }
    }

    @Test
    fun testUserInfo() {
        onView(withId(R.id.image_profile)).check(matches(withDrawable(R.drawable.profile)))
        onView(withId(R.id.text_name)).check(matches(withText("user1")))
        onView(withId(R.id.text_email)).check(matches(withText("user1@gmail.com")))
    }

    @Test
    fun testNavigation() {
        // Press Edit Your Profile should navigate to EditProfileFragment
        onView(withId(R.id.layout_edit)).perform(click())
        onView(withId(R.id.edit_profile_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.edit_profile_fragment) }

        // Press back should return AccountFragment
        pressBack()
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.account_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.account_fragment) }

        // Press Reminder should navigate to ReminderFragment
        onView(withId(R.id.layout_reminder)).perform(click())
        onView(withId(R.id.reminder_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.reminder_fragment) }

        // Press back should return AccountFragment
        pressBack()
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.bottom_nav)).check(matches(withCheckedItem(R.id.account_fragment)))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.account_fragment) }

        // Press Sign Out should navigate to SignInFragment
        onView(withId(R.id.layout_logout)).perform(click())
        onView(withId(R.id.sign_in_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController { assertThat(currentDestination?.id).isEqualTo(R.id.sign_in_fragment) }
    }
}