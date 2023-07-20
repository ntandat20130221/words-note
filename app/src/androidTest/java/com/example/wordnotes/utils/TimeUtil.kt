package com.example.wordnotes.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilTest {

    @Test
    fun testTimeAgo() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertEquals("now", timeAgo(context, System.currentTimeMillis()))
        assertEquals("now", timeAgo(context, System.currentTimeMillis() - (59 * 1000L)))

        assertEquals("1 minute", timeAgo(context, System.currentTimeMillis() - (60 * 1000L)))
        assertEquals("59 minutes", timeAgo(context, System.currentTimeMillis() - ( 59 * 60 * 1000L)))

        assertEquals("1 hour", timeAgo(context, System.currentTimeMillis() - (60 * 60 * 1000L)))
        assertEquals("23 hours", timeAgo(context, System.currentTimeMillis() - (23 * 60 * 60 * 1000L)))

        assertEquals("yesterday", timeAgo(context, System.currentTimeMillis() - (24 * 60 * 60 * 1000L)))
        assertEquals("6 days", timeAgo(context, System.currentTimeMillis() - (6 * 24 * 60 * 60 * 1000L)))

        assertEquals("1 week", timeAgo(context, System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)))
        assertEquals("3 weeks", timeAgo(context, System.currentTimeMillis() - (3 * 7 * 24 * 60 * 60 * 1000L)))

        assertEquals("1 month", timeAgo(context, System.currentTimeMillis() - (4 * 7 * 24 * 60 * 60 * 1000L)))
        assertEquals("11 months", timeAgo(context, System.currentTimeMillis() - (11 * 4 * 7 * 24 * 60 * 60 * 1000L)))

        assertEquals("1 year", timeAgo(context, System.currentTimeMillis() - (12 * 4 * 7 * 24 * 60 * 60 * 1000L)))
        assertEquals("25 years", timeAgo(context, System.currentTimeMillis() - (25 * 12 * 4 * 7 * 24 * 60 * 60 * 1000L)))

        assertEquals("5 hours", timeAgo(context, System.currentTimeMillis() - (5 * 60 * 60 * 1000L)))
    }
}