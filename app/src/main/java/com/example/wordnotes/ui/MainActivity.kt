package com.example.wordnotes.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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

interface BottomNavHideable

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @VisibleForTesting
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setUpNavigation()
        controlBottomNavVisibility()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun setUpNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        binding.bottomNav.apply {
            setupWithNavController(navController)
            setOnItemReselectedListener { /* Do nothing */ }
        }
    }

    private fun controlBottomNavVisibility() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, fragment: Fragment) {
                if (fragment is BottomNavHideable) {
                    setBottomNavVisibility(View.GONE)
                    resetBottomNavAnimation()
                }
            }

            override fun onFragmentStopped(fm: FragmentManager, fragment: Fragment) {
                if (fragment is BottomNavHideable) {
                    setBottomNavVisibility(View.VISIBLE)
                }
            }
        }, true)
    }

    fun setBottomNavVisibility(visibility: Int) {
        binding.bottomNav.visibility = visibility
    }

    fun slideOutBottomNav(duration: Long = 200, vararg relatedView: View) {
        ValueAnimator.ofInt(binding.bottomNav.height, 0).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = binding.bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                binding.bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    fun slideInBottomNav(duration: Long = 200, vararg relatedView: View) {
        ValueAnimator.ofInt(0, binding.bottomNav.height).apply {
            setDuration(duration)
            addUpdateListener { updatedAnimation ->
                val translationAmount = binding.bottomNav.height.toFloat() - updatedAnimation.animatedValue as Int
                binding.bottomNav.translationY = translationAmount
                relatedView.forEach { it.translationY = translationAmount }
            }
            start()
        }
    }

    fun resetBottomNavAnimation(vararg relatedView: View) {
        binding.bottomNav.translationY = 0f
        relatedView.forEach { it.translationY = 0f }
    }
}