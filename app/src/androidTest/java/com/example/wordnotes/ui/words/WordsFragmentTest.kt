package com.example.wordnotes.ui.words

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.wordnotes.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class WordsFragmentTest {
    private lateinit var navController: NavController
    private lateinit var fragmentScenario: FragmentScenario<WordsFragment>

    @Before
    fun setUpNavController() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_WordNotes) {
            WordsFragment().also { fragment ->
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
    fun clickItem_NavigateToWordDetailFragment() {
        onView(withId(R.id.words_recycler_view)).perform(actionOnItemAtPosition<WordsViewHolder>(0, click()))
        assertThat(navController.currentDestination?.id).isEqualTo(R.id.word_detail_fragment)
    }
}