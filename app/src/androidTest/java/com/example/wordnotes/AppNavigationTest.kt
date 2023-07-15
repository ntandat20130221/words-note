package com.example.wordnotes

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wordnotes.ui.MainActivity
import com.example.wordnotes.ui.words.WordsFragment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    @Test
    fun navigateAroundApp() {
        launch(MainActivity::class.java)

        onView(withId(R.id.fab_add_word)).perform(click())
        onView(withId(R.id.fab_add_word)).check(doesNotExist())
        onView(withText("AddEditWordFragment")).check(matches(isDisplayed()))

        onView(withId(R.id.settings_fragment)).perform(click())
        onView(withId(R.id.settings_fragment)).check(matches(isDisplayed()))
        onView(withText("AddEditWordFragment")).check(doesNotExist())

        onView(isRoot()).perform(ViewActions.pressBack())

    }

    @Test
    fun clickFab_ShouldOpenAddEditWordFragment() {
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