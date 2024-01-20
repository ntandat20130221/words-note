package com.example.wordnotes.testutils

import androidx.navigation.NavController
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.wordnotes.ui.MainActivity

fun ActivityScenarioRule<MainActivity>.withNavController(block: NavController.() -> Unit) {
    scenario.onActivity { activity ->
        block(activity.navController)
    }
}