package com.example.customviews.materialsearchview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.example.customviews.R
import com.example.customviews.materialsearchview.utils.dp

class CircularWaveform @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = DEF_STYLE_ATTR,
    defStyleRes: Int = DEF_STYLE_RES
) : View(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        private val DEF_STYLE_ATTR = R.attr.circularWaveformStyle
        private val DEF_STYLE_RES = R.style.Widget_CircularWaveform
    }

    private val buttonPaint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private val rmsPaint: Paint = Paint().apply {
        isAntiAlias = true
    }

    private val textPaint: Paint = TextPaint().apply {
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var onStopClicked: (() -> Unit)? = null
    private var rms: Float = 0f
    private var text: String = ""

    private val voiceIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_voice, null)
    private val voiceSize = 24.dp
    private val buttonRadius = 52.dp
    private var cx = 0f
    private var cy = 0f

    init {
        post {
            cx = width / 2f
            cy = 48.dp + buttonRadius
        }

        val themedContext = getContext()
        val a = themedContext.obtainStyledAttributes(attrs, R.styleable.CircularWaveform, defStyleAttr, defStyleRes)

        val backgroundColor = a.getColor(R.styleable.CircularWaveform_backgroundColor, themedContext.getColor(R.color.waveform_background))
        val voiceIconColor = a.getColor(R.styleable.CircularWaveform_voiceIconColor, themedContext.getColor(R.color.voice_icon_color))
        val rmsColor = a.getColor(R.styleable.CircularWaveform_rmsColor, themedContext.getColor(R.color.rms_color))
        val stopButtonColor = a.getColor(R.styleable.CircularWaveform_stopButtonColor, themedContext.getColor(R.color.stop_button_color))
        val textColor = a.getColor(R.styleable.CircularWaveform_android_textColor, themedContext.getColor(R.color.text_color))
        val textSize =
            a.getDimensionPixelSize(R.styleable.CircularWaveform_android_textSize, resources.getDimension(R.dimen.result_text_size).toInt())

        val backgroundDrawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_waveform, themedContext.theme)
        backgroundDrawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(backgroundColor, BlendModeCompat.SRC_ATOP)
        background = backgroundDrawable
        buttonPaint.color = stopButtonColor
        rmsPaint.color = rmsColor
        textPaint.color = textColor
        textPaint.textSize = textSize.toFloat()

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = resources.displayMetrics.heightPixels / 3
        val suppliedHeight = MeasureSpec.getSize(heightMeasureSpec)

        val height: Int = when (MeasureSpec.getSize(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> suppliedHeight
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(suppliedHeight)
            else -> desiredHeight
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Draw RMS
        val rmsRadius = buttonRadius + 3 * rms
        canvas?.drawOval(cx - rmsRadius, cy - rmsRadius, cx + rmsRadius, cy + rmsRadius, rmsPaint)

        // Draw stop button
        canvas?.drawOval(cx - buttonRadius, cy - buttonRadius, cx + buttonRadius, cy + buttonRadius, buttonPaint)

        // Draw voice icon
        canvas?.let {
            voiceIcon?.setBounds((cx - voiceSize).toInt(), (cy - voiceSize).toInt(), (cx + voiceSize).toInt(), (cy + voiceSize).toInt())
            voiceIcon?.draw(it)
        }

        // Draw text
        canvas?.drawText(text, cx, cy + buttonRadius + 64.dp, textPaint)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (isButtonTouched(event)) {
            onStopClicked?.invoke()
        }
        return super.dispatchTouchEvent(event)
    }

    private fun isButtonTouched(event: MotionEvent): Boolean {
        return event.x in cx - buttonRadius..cx + buttonRadius && event.y in cy - buttonRadius..cy + buttonRadius
    }

    fun setRms(rms: Float) {
        this.rms = rms
        invalidate()
    }

    fun setText(text: String) {
        this.text = text
        invalidate()
    }

    fun setOnStopRecording(callback: () -> Unit) {
        this.onStopClicked = callback
    }
}