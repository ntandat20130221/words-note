package com.example.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IPAKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_ATTR,
    defStyleRes: Int = 0
) : FrameLayout(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes) {

    companion object {
        private val DEF_STYLE_ATTR = R.attr.ipaKeyboardStyle
    }

    private var ic: InputConnection? = null
    private var normalColor: Int? = null
    private var pressedColor: Int? = null
    private var deletingJob: Job? = null
    private var isPressingDelete: Boolean = false

    init {
        val themedContext = getContext()
        LayoutInflater.from(themedContext).inflate(R.layout.ipa_keyboard, this, true)

        val a: TypedArray = themedContext.obtainStyledAttributes(
            0, intArrayOf(
                com.google.android.material.R.attr.colorSurfaceContainer,
                com.google.android.material.R.attr.colorSurfaceContainerLow
            )
        )

        normalColor = a.getColor(0, 0)
        @SuppressLint("ResourceType")
        pressedColor = a.getColor(1, 0)

        a.recycle()

        setKeyListeners(findViewById(R.id.keyboard))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setKeyListeners(viewGroup: ViewGroup) {
        for (child in viewGroup.children) {
            if (child is LinearLayout) {
                setKeyListeners(child)
            } else {
                val key = (child as FrameLayout).getChildAt(0)
                child.setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            pressedColor?.let { key.backgroundTintList = ColorStateList.valueOf(it) }
                            if (key.tag == "key_delete") {
                                deletingJob = viewScope.launch {
                                    delay(ViewConfiguration.getLongPressTimeout().toLong())
                                    isPressingDelete = true
                                    while (true) {
                                        ic?.deleteSurroundingText(1, 0)
                                        delay(50)
                                    }
                                }
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            normalColor?.let { key.backgroundTintList = ColorStateList.valueOf(it) }
                            deletingJob?.cancel()
                            when (key.tag) {
                                "key_normal" -> {
                                    if (key is TextView) {
                                        ic?.commitText(key.text, 1)
                                    }
                                }

                                "key_delete" -> {
                                    if (!isPressingDelete) {
                                        ic?.deleteSurroundingText(1, 0)
                                    }
                                    isPressingDelete = false
                                }

                                "key_done" -> ic?.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                            }
                        }
                    }
                    super.onTouchEvent(event)
                }
            }
        }
    }

    fun setInputConnection(ic: InputConnection) {
        this.ic = ic
    }
}