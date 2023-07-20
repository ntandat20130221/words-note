package com.example.wordnotes.utils

import android.content.Context
import com.example.wordnotes.R

const val SECOND_MILLIS = 1000L
const val MINUTE_MILLIS = 60L * SECOND_MILLIS
const val HOUR_MILLIS = 60L * MINUTE_MILLIS
const val DAY_MILLIS = 24L * HOUR_MILLIS
const val WEEK_MILLIS = 7L * DAY_MILLIS
const val MONTH_MILLIS = 4L * WEEK_MILLIS
const val YEAR_MILLIS = 12L * MONTH_MILLIS

fun timeAgo(context: Context, pastTime: Long): String? {
    val now = System.currentTimeMillis()
    if (pastTime > now || pastTime <= 0) return null

    val diff = now - pastTime
    return when {
        diff / YEAR_MILLIS > 0 -> (diff / YEAR_MILLIS).let {
            context.resources.getQuantityString(R.plurals.year, it.toInt(), it)
        }

        diff / MONTH_MILLIS > 0 -> (diff / MONTH_MILLIS).let {
            context.resources.getQuantityString(R.plurals.month, it.toInt(), it)
        }

        diff / WEEK_MILLIS > 0 -> (diff / WEEK_MILLIS).let {
            context.resources.getQuantityString(R.plurals.week, it.toInt(), it)
        }

        diff / DAY_MILLIS > 0 -> (diff / DAY_MILLIS).let {
            context.resources.getQuantityString(R.plurals.day, it.toInt(), it)
        }

        diff / HOUR_MILLIS > 0 -> (diff / HOUR_MILLIS).let {
            context.resources.getQuantityString(R.plurals.hour, it.toInt(), it)
        }

        diff / MINUTE_MILLIS > 0 -> (diff / MINUTE_MILLIS).let {
            context.resources.getQuantityString(R.plurals.minute, it.toInt(), it)
        }

        else -> context.resources.getString(R.string.now)
    }
}
