package com.example.wordnotes.testutils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.wordnotes.ui.MainActivity

fun getString(@StringRes stringRes: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.getString(stringRes, *args)
}

fun getQuantityString(@PluralsRes res: Int, quantity: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.resources.getQuantityString(res, quantity, *args)
}

fun ActivityScenarioRule<MainActivity>.withNavController(block: NavController.() -> Unit) {
    scenario.onActivity { activity ->
        block(activity.navController)
    }
}

fun isKeyboardOpened(): Boolean = UiDevice
    .getInstance(InstrumentationRegistry.getInstrumentation())
    .executeShellCommand("dumpsys input_method | grep mInputShown").contains("mInputShown=true")