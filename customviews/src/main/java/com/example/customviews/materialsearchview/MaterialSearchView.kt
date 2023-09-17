package com.example.customviews.materialsearchview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
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
import com.example.customviews.circularwaveform.CircularWaveform
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class MaterialSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_ATTR,
    defStyleRes: Int = DEF_STYLE_RES
) : FrameLayout(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes) {

    companion object {
        private val DEF_STYLE_ATTR = R.attr.materialSearchViewStyle
        private val DEF_STYLE_RES = R.style.Widget_MaterialSearchView
    }

    private val scrimView: View
    private val rootView: LinearLayout
    private val searchBar: LinearLayout
    private val buttonBack: ImageButton
    private val inputSearch: EditText
    private val buttonVoice: ImageButton
    private val buttonClear: ImageButton
    private val contentContainer: FrameLayout

    private var layoutInflated = false
    private var isVoiceIconEnabled = false
    private var currentTransitionState = TransitionState.HIDDEN

    private var onQueryTextChanged: ((String) -> Unit)? = null
    private var onVoiceClicked: (() -> Unit)? = null
    private val transitionListeners = LinkedHashSet<TransitionListener>()
    private val searchViewAnimationHelper: SearchViewAnimationHelper

    private val isVoiceAvailable: Boolean
        get() {
            @Suppress("DEPRECATION")
            val activities = context.packageManager.queryIntentActivities(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)
            return activities.size > 0
        }

    init {
        val themedContext = getContext()
        val a = themedContext.obtainStyledAttributes(attrs, R.styleable.MaterialSearchView, defStyleAttr, defStyleRes)

        val searchVoiceIcon = a.getResourceId(R.styleable.MaterialSearchView_searchVoiceIcon, R.drawable.ic_voice)
        val searchClearIcon = a.getResourceId(R.styleable.MaterialSearchView_searchClearIcon, R.drawable.ic_clear)
        val searchBackIcon = a.getResourceId(R.styleable.MaterialSearchView_searchBackIcon, R.drawable.ic_back)
        val searchBarHeight =
            a.getDimensionPixelSize(R.styleable.MaterialSearchView_searchBarHeight, resources.getDimension(R.dimen.search_bar_height).toInt())
        isVoiceIconEnabled = a.getBoolean(R.styleable.MaterialSearchView_searchVoiceIconEnabled, true)
        val hint = a.getString(R.styleable.MaterialSearchView_android_hint)
        val textSize = a.getDimensionPixelSize(R.styleable.MaterialSearchView_android_textSize, resources.getDimension(R.dimen.text_size).toInt())

        a.recycle()

        LayoutInflater.from(themedContext).inflate(R.layout.material_search_view, this, true)
        layoutInflated = true
        elevation = resources.getInteger(R.integer.search_view_elevation).toFloat()

        scrimView = findViewById(R.id.scrim_view)
        rootView = findViewById(R.id.search_view_root)
        searchBar = findViewById(R.id.search_bar)
        buttonBack = findViewById(R.id.button_back)
        inputSearch = findViewById(R.id.input_search)
        buttonVoice = findViewById(R.id.button_voice)
        buttonClear = findViewById(R.id.button_clear)
        contentContainer = findViewById(R.id.content_container)

        searchViewAnimationHelper = SearchViewAnimationHelper(this)

        buttonVoice.setImageResource(searchVoiceIcon)
        buttonClear.setImageResource(searchClearIcon)
        buttonBack.setImageResource(searchBackIcon)
        searchBar.layoutParams.height = searchBarHeight
        inputSearch.hint = hint
        inputSearch.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())

        buttonBack.setOnClickListener { onBackClicked() }
        buttonVoice.setOnClickListener { onVoiceClicked() }
        buttonClear.setOnClickListener { onClearClicked() }

        displayVoiceButton(true)
        setUpSearchInput()
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
        onQueryTextChanged?.let { onQueryTextChanged?.invoke(newText) }

        if (!TextUtils.isEmpty(newText)) {
            displayVoiceButton(false)
            displayClearButton(true)
        } else {
            displayVoiceButton(true)
            displayClearButton(false)
        }
    }

    private fun displayVoiceButton(display: Boolean) {
        buttonVoice.visibility = if (display && isVoiceIconEnabled && isVoiceAvailable) VISIBLE else GONE
    }

    private fun displayClearButton(display: Boolean) {
        buttonClear.visibility = if (display) VISIBLE else GONE
    }

    fun show() {
        if (currentTransitionState == TransitionState.SHOWN) return
        searchViewAnimationHelper.show()
        inputSearch.text = null
        requestFocusAndShowKeyboard(inputSearch)
    }

    fun hide() {
        if (currentTransitionState == TransitionState.HIDDEN) return
        searchViewAnimationHelper.hide()
        inputSearch.text = null
        clearFocusAndHideKeyboard(inputSearch)
    }

    private fun requestFocusAndShowKeyboard(view: View) {
        view.requestFocus()
        view.context.getSystemService(InputMethodManager::class.java)?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun clearFocusAndHideKeyboard(view: View) {
        view.clearFocus()
        view.context.getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setTransitionState(state: TransitionState) {
        if (currentTransitionState == state) return

        val previousState = currentTransitionState
        currentTransitionState = state
        val listeners = LinkedHashSet(transitionListeners)
        listeners.forEach { it.onStateChanged(this, previousState, state) }
    }

    fun isShowing() = currentTransitionState == TransitionState.SHOWN

    private fun onBackClicked() = hide()

    private fun onVoiceClicked() = onVoiceClicked?.let { onVoiceClicked?.invoke() }

    fun listenInput() {
        if (!isVoiceIconEnabled || !isVoiceAvailable) return

        clearFocusAndHideKeyboard(inputSearch)

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionAdapter() {
            private lateinit var waveform: CircularWaveform
            private var dialog: BottomSheetDialog? = null

            override fun onReadyForSpeech(params: Bundle?) {
                dialog = BottomSheetDialog(context).apply {
                    waveform = CircularWaveform(context, null)
                    setContentView(waveform)
                    setOnCancelListener { speechRecognizer.destroy() }
                    show()
                }

                waveform.apply {
                    setOnStopRecording { dialog?.cancel() }
                    setText(resources.getString(R.string.listening))
                }
            }

            override fun onRmsChanged(rmsdB: Float) {
                waveform.setRms(rmsdB)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                text?.let {
                    waveform.setText(it)
                }
            }

            override fun onError(error: Int) {
                speechRecognizer.destroy()
            }

            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.get(0)
                text?.let {
                    waveform.setText(it)
                    if (waveform.viewTreeObserver.isAlive) {
                        waveform.viewTreeObserver.addOnDrawListener {
                            inputSearch.setText(it)
                            dialog?.cancel()
                        }
                    }
                }
                speechRecognizer.destroy()
            }
        })

        speechRecognizer.startListening(intent)
    }

    private fun onClearClicked() {
        inputSearch.text = null
    }

    private fun setVisible(visible: Boolean) {
        rootView.visibility = if (visible) VISIBLE else GONE
        setTransitionState(if (visible) TransitionState.SHOWN else TransitionState.HIDDEN)
    }

    fun setOnQueryTextListener(onQueryTextChanged: (String) -> Unit) {
        this.onQueryTextChanged = onQueryTextChanged
    }

    fun setOnVoiceClickedListener(onVoiceClicked: () -> Unit) {
        this.onVoiceClicked = onVoiceClicked
    }

    fun addTransitionListener(listener: TransitionListener) = transitionListeners.add(listener)

    fun interface TransitionListener {
        fun onStateChanged(searchView: MaterialSearchView, previousState: TransitionState, newState: TransitionState)
    }

    enum class TransitionState { HIDING, HIDDEN, SHOWING, SHOWN }

    override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()?.let {
            val state = SavedState(it)
            state.text = inputSearch.text.toString()
            state.visibility = rootView.visibility
            return state
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                inputSearch.setText(state.text)
                setVisible(state.visibility == VISIBLE)
            }

            else -> super.onRestoreInstanceState(state)
        }
    }

    class SavedState : BaseSavedState {
        var text: String? = null
        var visibility = 0

        constructor(source: Parcel, classLoader: ClassLoader? = null) : super(source, classLoader) {
            text = source.readString()
            visibility = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeString(text)
            dest.writeInt(visibility)
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Creator<SavedState> = object : Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState> = newArray(size)
            }
        }
    }
}