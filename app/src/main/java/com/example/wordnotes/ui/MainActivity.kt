package com.example.wordnotes.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.wordnotes.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        findViewById<BottomNavigationView>(R.id.bottom_nav).apply {
            setupWithNavController(navController)
            setOnItemReselectedListener { menuItem ->
                // https://stackoverflow.com/questions/60041437/bottom-navigation-in-android-works-only-on-double-clicks
                if (menuItem.itemId == R.id.words_fragment && navController.currentDestination?.id == R.id.add_edit_word_fragment)
                    navController.popBackStack()
            }
            setOnItemSelectedListener { item ->
                // https://stackoverflow.com/questions/71089052/android-navigation-component-bottomnavigationviews-selected-tab-icon-is-not-u
                NavigationUI.onNavDestinationSelected(item, navController)
                true
            }
        }
    }
}