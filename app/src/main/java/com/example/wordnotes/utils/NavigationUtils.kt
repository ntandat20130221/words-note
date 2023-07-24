package com.example.wordnotes.utils

import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

fun NavController.setUpToolbar(toolbar: Toolbar, appBarConfiguration: AppBarConfiguration = AppBarConfiguration(navGraph = graph)) {
    toolbar.setupWithNavController(this, appBarConfiguration)
}