package com.example.wordnotes.utils

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

@ColorInt
fun Context.themeColor(@AttrRes attrRes: Int): Int = TypedValue()
    .apply { theme.resolveAttribute(attrRes, this, true) }
    .data

fun NavController.setUpToolbar(toolbar: Toolbar, appBarConfiguration: AppBarConfiguration = AppBarConfiguration(navGraph = graph)) {
    toolbar.setupWithNavController(this, appBarConfiguration)
}

fun Context.isNetworkAvailable(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java)
    val network = cm.activeNetwork ?: return false
    val networkCap = cm.getNetworkCapabilities(network) ?: return false
    return when {
        networkCap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        networkCap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        networkCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        networkCap.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}

val Number.toPx get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)