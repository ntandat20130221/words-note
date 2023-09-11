package com.example.wordnotes.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.Window
import androidx.annotation.ColorInt

fun Window.fadeOutStatusBar(
    @ColorInt fromColor: Int = context.themeColor(com.google.android.material.R.attr.colorSurfaceContainer),
    @ColorInt toColor: Int = context.themeColor(com.google.android.material.R.attr.colorSurface),
    duration: Long = 300
) {
    ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
        setDuration(duration)
        addUpdateListener {
            statusBarColor = it.animatedValue as Int
        }
        start()
    }
}

fun Window.fadeInStatusBar(
    @ColorInt fromColor: Int = context.themeColor(com.google.android.material.R.attr.colorSurface),
    @ColorInt toColor: Int = context.themeColor(com.google.android.material.R.attr.colorSurfaceContainer),
    duration: Long = 300
) {
    ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
        setDuration(duration)
        addUpdateListener {
            statusBarColor = it.animatedValue as Int
        }
        start()
    }
}