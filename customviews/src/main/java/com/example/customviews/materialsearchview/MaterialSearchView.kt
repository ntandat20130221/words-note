package com.example.customviews.materialsearchview

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import com.example.customviews.R

class MaterialSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var scrimView: View
    private var rootView: LinearLayout
    private var searchBar: LinearLayout
    private var buttonBack: ImageButton
    private var inputSearch: EditText
    private var buttonVoice: ImageButton
    private var buttonClear: ImageButton
    private var contentContainer: FrameLayout

    private var layoutInflated = false
    private var isVoiceIconEnabled = false
    private var currentTransitionState = TransitionState.HIDDEN

    private var onQueryTextListener: OnQueryTextListener? = null
    private val transitionListeners = LinkedHashSet<TransitionListener>()

    private val isVoiceAvailable: Boolean
        get() {
            @Suppress("DEPRECATION")
            val activities = context.packageManager.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
            return activities.size > 0
        }

    init {
        val themedContext = getContext()
        val a = themedContext.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, defStyleAttr, defStyleRes)
        isVoiceIconEnabled = a.getBoolean(R.styleable.MaterialSearchView_voiceIconEnabled, true)
        a.recycle()

        LayoutInflater.from(themedContext).inflate(R.layout.material_search_view, this, true)
        layoutInflated = true
        elevation = 100f

        scrimView = findViewById(R.id.scrim_view)
        rootView = findViewById(R.id.search_view_root)
        searchBar = findViewById(R.id.search_bar)
        buttonBack = findViewById(R.id.button_back)
        inputSearch = findViewById(R.id.input_search)
        buttonVoice = findViewById(R.id.button_voice)
        buttonClear = findViewById(R.id.button_clear)
        contentContainer = findViewById(R.id.content_container)

        buttonBack.setOnClickListener { onBackClicked() }
        buttonVoice.setOnClickListener { onVoiceClicked() }
        buttonClear.setOnClickListener { onClearClicked() }

        setUpSearchInput()

        displayVoiceButton(true)
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (layoutInflated) {
            contentContainer.addView(child, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

    private fun setUpSearchInput() {
        inputSearch.doOnTextChanged { text, _, _, _ ->
            onTextChanged(text.toString())
        }
    }

    private fun onTextChanged(newText: String) {
        onQueryTextListener?.onQueryTextChanged(newText)

        if (!TextUtils.isEmpty(newText)) {
            displayVoiceButton(false)
            displayClearButton(true)
        } else {
            displayVoiceButton(true)
            displayClearButton(false)
        }
    }

    private fun displayVoiceButton(display: Boolean) {
        buttonVoice.visibility = if (display && isVoiceAvailable && isVoiceIconEnabled) VISIBLE else GONE
    }

    private fun displayClearButton(display: Boolean) {
        buttonClear.visibility = if (display) VISIBLE else GONE
    }

    fun show() {
        if (currentTransitionState == TransitionState.SHOWN) return
        requestFocusAndShowKeyboard(inputSearch)
        rootView.visibility = VISIBLE
        setTransitionState(TransitionState.SHOWN)
    }

    fun hide() {
        if (currentTransitionState == TransitionState.HIDDEN) return
        clearFocusAndHideKeyboard(inputSearch)
        rootView.visibility = GONE
        setTransitionState(TransitionState.HIDDEN)
    }

    private fun requestFocusAndShowKeyboard(view: View) {
        view.requestFocus()
        view.context.getSystemService(InputMethodManager::class.java)?.showSoftInput(view, 0)
    }

    private fun clearFocusAndHideKeyboard(view: View) {
        view.clearFocus()
        view.context.getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setTransitionState(state: TransitionState) {
        if (currentTransitionState == state) return

        val previousState = currentTransitionState
        currentTransitionState = state
        val listeners = LinkedHashSet(transitionListeners)
        listeners.forEach { it.onStateChanged(this, previousState, state) }
    }

    fun isShowing() = currentTransitionState == TransitionState.SHOWN

    private fun onBackClicked() = hide()

    private fun onVoiceClicked() {

    }

    private fun onClearClicked() {
        inputSearch.text = null
    }

    fun setVoiceEnabled(enable: Boolean) {
        isVoiceIconEnabled = enable
    }

    fun setOnQueryTextListener(onQueryTextListener: OnQueryTextListener?) {
        this.onQueryTextListener = onQueryTextListener
    }

    fun addTransitionListener(listener: TransitionListener) = transitionListeners.add(listener)

    fun removeTransitionListener(listener: TransitionListener) = transitionListeners.remove(listener)

    fun interface OnQueryTextListener {
        fun onQueryTextChanged(query: String)
    }

    fun interface TransitionListener {
        fun onStateChanged(searchView: MaterialSearchView, previousState: TransitionState, newState: TransitionState)
    }

    enum class TransitionState { HIDDEN, SHOWN }
}