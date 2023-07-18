package com.example.wordnotes.ui.addeditword

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.wordnotes.R
import org.junit.Before

class AddEditWordFragmentTest {
    private lateinit var navController: NavController
    private lateinit var fragmentScenario: FragmentScenario<AddEditWordFragment>

    @Before
    fun setUpNavController() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        fragmentScenario = launchFragmentInContainer(themeResId = R.style.Theme_WordNotes, fragmentArgs = null) {
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
}