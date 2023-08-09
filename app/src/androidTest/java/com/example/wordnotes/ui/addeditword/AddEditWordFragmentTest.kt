package com.example.wordnotes.ui.addeditword

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isNotSelected
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.wordnotes.R
import com.example.wordnotes.testutils.atPosition
import org.junit.Before
import org.junit.Test

class AddEditWordFragmentTest {
    private lateinit var navController: NavController
    private lateinit var fragmentScenario: FragmentScenario<AddEditWordFragment>

    @Before
    fun setUpNavController() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_WordNotes, fragmentArgs = bundleOf()) {
            AddEditWordFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        navController.setGraph(R.navigation.nav_graph)
                        Navigation.setViewNavController(fragment.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun atInitial_PosRecycler_ShouldAtVerbItem() {
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(0, isSelected())))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(1, isNotSelected())))
        onView(withId(R.id.pos_recycler_view)).check(matches(atPosition(2, isNotSelected())))
    }

    @Test
    fun fillSomeInputs_Recreate_PersistUiState() {
        onView(withId(R.id.input_word)).perform(click(), typeText("word"))
        onView(withId(R.id.check_remind)).perform(click())

        fragmentScenario.recreate()

        onView(withId(R.id.input_word)).check(matches(withText("word")))
        onView(withId(R.id.check_remind)).check(matches(isChecked()))
    }
}