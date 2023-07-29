package com.example.wordnotes

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Checkable
import androidx.annotation.AttrRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import com.example.wordnotes.utils.themeColor
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.Description
import org.hamcrest.Matcher

fun getString(@StringRes stringRes: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.getString(stringRes, *args)
}

fun getQuantityString(@PluralsRes res: Int, quantity: Int, vararg args: Any): String {
    val context = ApplicationProvider.getApplicationContext<Context>()
    return context.resources.getQuantityString(res, quantity, *args)
}

fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> = object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("has item at position $position: ")
        itemMatcher.describeTo(description)
    }

    override fun matchesSafely(view: RecyclerView): Boolean {
        val viewHolder = view.findViewHolderForAdapterPosition(position) ?: return false
        return itemMatcher.matches(viewHolder.itemView)
    }
}

fun withBackgroundColor(@AttrRes colorAttr: Int): Matcher<View> = object : BoundedMatcher<View, View>(View::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with text color: ")
    }

    override fun matchesSafely(item: View): Boolean {
        val colorDrawable = item.background as? ColorDrawable ?: return false
        return colorDrawable.color == item.context.themeColor(colorAttr)
    }
}

fun setChecked(checked: Boolean): ViewAction = object : ViewAction {
    override fun getDescription(): String = "action check"

    override fun getConstraints(): Matcher<View> = object : BaseMatcher<View>() {
        override fun describeTo(description: Description?) {}
        override fun matches(actual: Any?): Boolean = isA(Checkable::class.java).matches(actual)
    }

    override fun perform(uiController: UiController?, view: View?) {
        (view as Checkable).isChecked = checked
    }
}