package com.example.wordnotes.testutils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider

fun getString(@StringRes stringRes: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.getString(stringRes, *args)
}

fun getQuantityString(@PluralsRes res: Int, quantity: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.resources.getQuantityString(res, quantity, *args)
}