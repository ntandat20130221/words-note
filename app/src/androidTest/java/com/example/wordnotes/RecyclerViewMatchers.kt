package com.example.wordnotes

import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import com.example.wordnotes.utils.themeColor
import org.hamcrest.Description
import org.hamcrest.Matcher

fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("has item at position $position: ")
            itemMatcher.describeTo(description)
        }

        override fun matchesSafely(view: RecyclerView): Boolean {
            val viewHolder = view.findViewHolderForAdapterPosition(position)
            return if (viewHolder == null) false else itemMatcher.matches(viewHolder.itemView)
        }
    }
}

fun withBackgroundColor(@AttrRes colorAttr: Int): Matcher<View> {
    return object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with text color: ");
        }

        override fun matchesSafely(item: View): Boolean {
            val colorDrawable = item.background as ColorDrawable
            return colorDrawable.color == item.context.themeColor(colorAttr)
        }
    }
}