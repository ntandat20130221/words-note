package com.example.wordnotes.ui.account.profile

import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.wordnotes.R
import com.example.wordnotes.data.FirebaseAuthWrapper
import com.example.wordnotes.di.FirebaseModule
import com.example.wordnotes.mocks.TestFirebaseAuthWrapper
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
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

@UninstallModules(FirebaseModule::class)
@HiltAndroidTest
class EditProfileFragmentTest {

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
    fun signInAndNavigateToEditProfileFragment() {
        onView(withId(R.id.input_email)).perform(typeText("user1@gmail.com"))
        onView(withId(R.id.input_password)).perform(typeText("111111"))
        onView(withId(R.id.button_sign_in)).perform(click())
        onView(withId(R.id.account_fragment)).perform(click())
        onView(withId(R.id.layout_edit)).perform(click())
    }

    @Test
    fun checkIfTheCurrentScreenIsEditProfileFragment() {
        onView(withId(R.id.edit_profile_fragment_layout)).check(matches(isDisplayed()))
        activityScenarioRule.withNavController {
            assertThat(currentDestination?.id).isEqualTo(R.id.edit_profile_fragment)
        }
    }

    @Test
    fun testUserInfo() {
        onView(withId(R.id.image_profile)).check(matches(withDrawable(R.drawable.profile)))
        onView(withId(R.id.input_username)).check(matches(withText("user1")))
        onView(withId(R.id.input_email)).check(matches(withText("user1@gmail.com")))
        onView(withId(R.id.input_phone)).check(matches(withHint("Your phone number")))
        onView(withId(R.id.input_gender)).check(matches(withHint("Your gender")))
        onView(withId(R.id.input_dob)).check(matches(withHint("Your date of birth")))
    }

    @Test
    fun updateProfileShouldSuccessful() {
        fillInformation()

        // Commit changes then check state
        onView(withId(R.id.menu_done)).perform(click())
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.image_profile)).check(matches(not(withDrawable(R.drawable.profile))))
        onView(withId(R.id.text_name)).check(matches(withText("updated_username")))
        onView(withId(R.id.text_email)).check(matches(withText("user1@gmail.com")))
    }

    @Test
    fun updateProfileButNotSaveThenPressBackThenCheckAccountFragmentStateShouldUnchanged() {
        onView(withId(R.id.input_username)).perform(replaceText("updated_username"))
        closeSoftKeyboard()
        pressBack()
        onView(withId(R.id.account_fragment_layout)).check(matches(isDisplayed()))
        onView(withId(R.id.text_name)).check(matches(withText("user1")))
    }

    @Test
    fun tryEditEmailShouldShowError() {
        onView(withId(R.id.input_email)).perform(click(), replaceText("updated_email@gmail.com"))
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(matches(withText(R.string.you_cant_change_email_address)))
    }

    @Test
    fun updateProfileThenRecreateActivityThenCheckUiStateShouldUnchanged() {
        // Fill information, open gender dialog then recreate activity
        val uiDevice = fillInformation()
        onView(withId(R.id.input_gender)).perform(click())
        activityScenarioRule.scenario.recreate()

        // Check if gender dialog is still display
        onView(withText("Choose gender")).check(matches(isDisplayed()))
        onData(allOf(instanceOf(String::class.java), `is`("Male"))).check(matches(isChecked()))
        uiDevice.wait(Until.hasObject(By.res("android:id/button1")), 1000)
        onView(withId(android.R.id.button1)).perform(click())
        uiDevice.wait(Until.hasObject(By.res("com.example.wordnotes:id/image_profile")), 1000)

        // Check if ui state should unchanged
        onView(withId(R.id.image_profile)).check(matches(not(withDrawable(R.drawable.profile))))
        onView(withId(R.id.input_username)).check(matches(withText("updated_username")))
        onView(withId(R.id.input_phone)).check(matches(withText("0123456789")))
        onView(withId(R.id.input_gender)).check(matches(withText("Male")))
        onView(withId(R.id.input_dob)).check(matches(withText("24/01/2024")))
    }

    private fun fillInformation(): UiDevice {
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Choose profile image
        onView(withId(R.id.view_avatar_outline)).perform(click())
        uiDevice.findObject(By.res("com.google.android.gms.optional_photopicker:id/icon_thumbnail")).click()

        // Change user info
        onView(withId(R.id.input_username)).perform(replaceText("updated_username"))
        onView(withId(R.id.input_phone)).perform(replaceText("0123456789"))

        // Choose gender
        onView(withId(R.id.input_gender)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("Male"))).perform(click())
        onView(withId(android.R.id.button1)).perform(click())

        // Choose date of birth
        onView(withId(R.id.input_dob)).perform(click())
        val yearMonthButton = uiDevice.findObject(By.res("com.example.wordnotes:id/month_navigation_fragment_toggle"))
        yearMonthButton.clickAndWait(Until.newWindow(), 500)
        uiDevice.findObject(By.clazz("android.widget.TextView").text("2024")).click()
        val nextButton = uiDevice.findObject(By.res("com.example.wordnotes:id/month_navigation_next"))
        while (!yearMonthButton.text.contains("January")) {
            nextButton.click()
        }
        uiDevice.findObject(By.clazz("android.widget.TextView").text("24")).click()
        uiDevice.wait(Until.hasObject(By.res("com.example.wordnotes:id/confirm_button").enabled(true)), 1000)
        uiDevice.findObject(By.res("com.example.wordnotes:id/confirm_button")).click()
        uiDevice.wait(Until.hasObject(By.res("com.example.wordnotes:id/input_dob")), 1000)
        onView(withId(R.id.input_dob)).check(matches(withText("24/01/2024")))

        return uiDevice
    }
}