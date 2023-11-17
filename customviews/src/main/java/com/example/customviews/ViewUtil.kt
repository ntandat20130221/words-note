package com.example.customviews

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

val Number.dp: Float get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)

val Number.sp: Float get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)

val View.viewScope: CoroutineScope
    get() {
        val storedScope = getTag(R.string.view_coroutine_scope) as? CoroutineScope
        if (storedScope != null) return storedScope

        return ViewCoroutineScope().also { newScope ->
            if (isAttachedToWindow) {
                addOnAttachStateChangeListener(newScope)
                setTag(R.string.view_coroutine_scope, newScope)
            } else {
                newScope.cancel()
            }
        }
    }

private class ViewCoroutineScope : CoroutineScope, View.OnAttachStateChangeListener {
    override val coroutineContext = SupervisorJob() + Dispatchers.Main

    override fun onViewAttachedToWindow(view: View) = Unit

    override fun onViewDetachedFromWindow(view: View) {
        coroutineContext.cancel()
        view.setTag(R.string.view_coroutine_scope, null)
    }
}