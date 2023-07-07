package com.example.wordnotes

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wordnotes.ui.words.WordsFragment
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Test
    fun testNavigation() {
        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        val wordsScenario = launchFragmentInContainer<WordsFragment>()

        Espresso.onView(ViewMatchers.withId(com.google.android.material.R.id.accelerate))
    }
}