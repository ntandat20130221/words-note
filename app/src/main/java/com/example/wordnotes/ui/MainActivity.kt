package com.example.wordnotes.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wordnotes.R
import com.example.wordnotes.databinding.ActivityMainBinding
import com.example.wordnotes.ui.account.AccountFragment
import com.example.wordnotes.ui.home.HomeFragment
import com.example.wordnotes.ui.learning.LearningFragment
import com.example.wordnotes.ui.worddetail.WordDetailFragment
import dagger.hilt.android.AndroidEntryPoint

const val KEY_START_DESTINATION_ID = "start_des_id"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment

    @VisibleForTesting
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setUpNavigation()
        controlBottomNavVisibility()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_START_DESTINATION_ID, navController.graph.startDestinationId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        navController.graph.setStartDestination(savedInstanceState.getInt(KEY_START_DESTINATION_ID))
    }

    private fun setUpNavigation() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNav.apply {
            setupWithNavController(navController)
            setOnItemReselectedListener { /* Do nothing */ }
        }

        navController.addOnDestinationChangedListener { _, destination, args ->
            when (destination.id) {
                R.id.home_fragment -> destination.label = getString(R.string.words)
                R.id.add_edit_word_fragment -> destination.label = if (args?.getString("wordId") == null)
                    getString(R.string.add_new_word) else getString(R.string.edit_word)

                R.id.learning_fragment -> destination.label = getString(R.string.learning)
                R.id.flash_card_fragment -> destination.label = getString(R.string.learning)
                R.id.reminder_fragment -> destination.label = getString(R.string.reminder)
                R.id.account_fragment -> destination.label = getString(R.string.account)
                R.id.edit_profile_fragment -> destination.label = getString(R.string.edit_profile)
                R.id.forgot_password_fragment -> destination.label = getString(R.string.forgot_password)
            }
        }
    }

    /**
     * Hide BottomNavigationView if the current fragment is not HomeFragment, LearningFragment, AccountFragment and
     * WordDetailFragment.
     */
    private fun controlBottomNavVisibility() {
        navHostFragment.childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, fragment: Fragment) {
                if (fragment::class !in listOf(HomeFragment::class, LearningFragment::class, AccountFragment::class, WordDetailFragment::class)) {
                    binding.bottomNav.visibility = View.GONE
                    binding.bottomNav.animate().translationX(0f).translationY(0f)
                }
            }

            override fun onFragmentStopped(fm: FragmentManager, fragment: Fragment) {
                if (fragment::class !in listOf(HomeFragment::class, LearningFragment::class, AccountFragment::class, WordDetailFragment::class)) {
                    binding.bottomNav.visibility = View.VISIBLE
                    binding.bottomNav.animate().translationX(0f).translationY(0f)
                }
            }
        }, false)
    }
}