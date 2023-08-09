package com.example.wordnotes.ui

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wordnotes.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    @VisibleForTesting
    lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpNavigation()
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav).also {
            it.setupWithNavController(navController)
            it.setOnItemReselectedListener { /* Do nothing */ }
        }
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        bottomNav.visibility = visibility
    }
}