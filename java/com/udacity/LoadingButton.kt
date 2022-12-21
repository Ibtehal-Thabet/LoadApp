package com.udacity

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var bgColor: Int = Color.BLACK
    private var txtColor: Int = Color.BLACK
    private lateinit var buttonText: String

    @Volatile
    private var progressLine: Double = 0.0
    private var progressCircle: Double = 0.0
    private var valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                buttonText = resources.getString(R.string.button_name)
                progressLine = 0.0
                progressCircle = 0.0
                invalidate()
            }
            ButtonState.Loading -> {
                isEnabled = false
                isClickable = true
                valueAnimator = AnimatorInflater.loadAnimator(
                    context,
                    R.animator.loading_animation
                ) as ValueAnimator

                valueAnimator.addUpdateListener(updateListener)
                valueAnimator.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        progressLine = 0.0
                        progressCircle = 0.0
                        if (buttonState == ButtonState.Loading)
                            buttonState = ButtonState.Completed
                    }
                })
                invalidate()
            }
            ButtonState.Completed -> {
                isEnabled = true
            }
        }

    }

    private val updateListener = ValueAnimator.AnimatorUpdateListener {
        progressLine = (it.animatedValue as Float).toDouble()
        progressCircle = (it.animatedValue as Float).toDouble()
        invalidate()
        requestLayout()
    }

    fun completedDownload() {
        valueAnimator.cancel()
        buttonState = ButtonState.Completed
        valueAnimator.removeUpdateListener(updateListener)
        invalidate()
        requestLayout()
    }


    init {
        val attr = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0,
            0
        )
        try {
            bgColor = attr.getColor(
                R.styleable.LoadingButton_bgColor,
                ContextCompat.getColor(context, R.color.colorPrimary)
            )

            txtColor = attr.getColor(
                R.styleable.LoadingButton_textColor,
                ContextCompat.getColor(context, R.color.white)
            )
        } finally {
            attr.recycle()
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed)
            buttonState = ButtonState.Loading
        valueAnimator.start()
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.strokeWidth = 0f
        paint.color = bgColor

        //draw animated rectangle
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        if (buttonState == ButtonState.Loading) {
            paint.color = Color.parseColor("#004349")
            canvas?.drawRect(
                0f, 0f,
                (width * (progressLine / 100)).toFloat(), height.toFloat(), paint
            )
        }

        //draw text
         buttonText = if (buttonState == ButtonState.Loading)
            resources.getString(R.string.button_loading)
        else resources.getString(R.string.button_name)

        paint.color = txtColor
        canvas?.drawText(buttonText, (width / 2).toFloat(), ((height + 30) / 2).toFloat(), paint)

        //draw animated circle
        if (buttonState == ButtonState.Loading) {
            paint.color = ContextCompat.getColor(context, R.color.colorAccent)
            canvas?.translate(widthSize / 2 + heightSize / 2 + resources.getDimension(R.dimen.default_text_size)*2, heightSize / 2 - resources.getDimension(R.dimen.default_text_size) / 2)
            canvas?.drawArc(
                0f, 0f,
                resources.getDimension(R.dimen.default_text_size),
                resources.getDimension(R.dimen.default_text_size),
                0f,
                (360*progressCircle/100).toFloat(), true, paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }
}