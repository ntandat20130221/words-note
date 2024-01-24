package com.example.wordnotes.testutils

import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.Checkable
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toColor
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.BoundedMatcher
import com.example.wordnotes.utils.themeColor
import com.google.android.material.navigation.NavigationBarView
import com.google.common.truth.Truth.assertThat
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

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
        description.appendText("with background color: ")
    }

    override fun matchesSafely(item: View): Boolean {
        val colorDrawable = item.background as? ColorDrawable ?: return false
        return colorDrawable.color == item.context.themeColor(colorAttr)
    }
}

@Suppress("unused")
fun setChecked(checked: Boolean): ViewAction = object : ViewAction {
    override fun getDescription(): String = "action check"

    override fun getConstraints(): Matcher<View> = object : BaseMatcher<View>() {
        override fun describeTo(description: Description?) {}
        override fun matches(actual: Any?): Boolean = CoreMatchers.isA(Checkable::class.java).matches(actual)
    }

    override fun perform(uiController: UiController?, view: View?) {
        (view as Checkable).isChecked = checked
    }
}

fun withCheckedItem(checkedItemId: Int): Matcher<View> {
    return object : BoundedMatcher<View, NavigationBarView>(NavigationBarView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("withCheckedItem: ")
        }

        override fun matchesSafely(view: NavigationBarView): Boolean {
            view.menu.forEach { menu ->
                if (menu.isChecked and (menu.itemId == checkedItemId)) {
                    return true
                }
            }
            return false
        }
    }
}

fun hasItemCount(expectedCount: Int): ViewAssertion = ViewAssertion { view, noViewFoundException ->
    noViewFoundException?.let { throw noViewFoundException }
    val actualCount = (view as RecyclerView).adapter?.itemCount
    assertThat(expectedCount).isEqualTo(actualCount)
}

fun withDrawable(
    @DrawableRes id: Int,
    @ColorRes tint: Int? = null,
    tintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN
) =
    object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with drawable same as drawable with id $id")
            tint?.let { description.appendText(", tint color id: $tint, mode: $tintMode") }
        }

        override fun matchesSafely(view: View): Boolean {
            val context = view.context
            val expectedBitmap = context.getDrawable(id)?.apply {
                tint?.let {
                    setTint(tint)
                    setTintMode(tintMode)
                }
            }
                ?.toBitmap()
            return view is ImageView && view.drawable.toBitmap().sameAs(expectedBitmap)
        }
    }