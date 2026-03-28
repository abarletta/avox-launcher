package com.avox.launcher

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.PI

class AlphabetSidebar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val STYLE_WAVE = "wave"
        const val STYLE_HIGHLIGHT = "highlight"
        const val STYLE_FADE = "fade"
    }

    private val items = mutableListOf<String>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    var onLetterSelected: ((String) -> Unit)? = null
    private var fontFamily: String = "sans-serif-light"

    // Animation state
    private var isTouching = false
    private var touchY = 0f
    private var animStrength = 0f // 0..1, animated in/out

    // Configurable parameters
    private var animStyle: String = STYLE_WAVE
    private var waveRadius = 3
    private var maxShiftPx = 25f
    private var maxScaleFactor = 1.8f
    private var highlightIntensity = 0.5f
    private var fadeRadiusFactor = 1.0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 200
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            animStrength = it.animatedValue as Float
            invalidate()
        }
    }

    fun setItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        invalidate()
    }

    fun setFontFamily(family: String) {
        fontFamily = family
        invalidate()
    }

    fun setAnimationStyle(style: String) {
        animStyle = style
        invalidate()
    }

    fun setWaveShift(shift: Float) {
        maxShiftPx = shift
        invalidate()
    }

    fun setWaveScale(scale: Float) {
        maxScaleFactor = scale
        invalidate()
    }

    fun setWaveRadius(radius: Int) {
        waveRadius = radius
        invalidate()
    }

    fun setHighlightIntensity(intensity: Float) {
        highlightIntensity = intensity
        invalidate()
    }

    fun setFadeRadius(factor: Float) {
        fadeRadiusFactor = factor
        invalidate()
    }

    private fun getTouchIndex(availableHeight: Float): Int {
        if (!isTouching && animStrength <= 0f) return -1
        val relativeY = touchY - paddingTop
        return (relativeY / availableHeight * items.size).toInt().coerceIn(0, items.size - 1)
    }

    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) return

        val availableHeight = (height - paddingTop - paddingBottom).toFloat()
        val itemHeight = availableHeight / items.size
        @Suppress("DEPRECATION")
        val baseTextSize = 14f * resources.displayMetrics.scaledDensity
        val typeface = Typeface.create(fontFamily, Typeface.NORMAL)
        val baseX = width / 2f
        val touchIndex = getTouchIndex(availableHeight)

        when (animStyle) {
            STYLE_WAVE -> drawWave(canvas, touchIndex, baseX, baseTextSize, typeface, itemHeight)
            STYLE_HIGHLIGHT -> drawHighlight(canvas, touchIndex, baseX, baseTextSize, typeface, itemHeight)
            STYLE_FADE -> drawFade(canvas, touchIndex, baseX, baseTextSize, typeface, itemHeight)
            else -> drawWave(canvas, touchIndex, baseX, baseTextSize, typeface, itemHeight)
        }
    }

    private fun drawWave(canvas: Canvas, touchIndex: Int, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)
            var scale = 1f
            var shiftX = 0f

            if (touchIndex >= 0 && animStrength > 0f) {
                val dist = abs(i - touchIndex)
                if (dist <= waveRadius) {
                    val h = (i - touchIndex).toDouble()
                    val t = h / (waveRadius + 1).toDouble() * (PI / 2.0)
                    val influenceD = cos(t) * exp(-5.0 * t * t) * animStrength.toDouble()
                    val influence = influenceD.toFloat()
                    scale = 1f + (maxScaleFactor - 1f) * influence
                    shiftX = -maxShiftPx * influence
                }
            }

            paint.textSize = baseTextSize * scale
            paint.typeface = typeface
            paint.alpha = if (touchIndex >= 0 && animStrength > 0f) {
                val dist = abs(i - touchIndex)
                if (dist <= waveRadius) 255
                else max(120, (255 * (1f - animStrength * 0.4f)).toInt())
            } else 255

            val x = baseX + shiftX
            val y = centerY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], x, y, paint)
        }
    }

    private fun drawHighlight(canvas: Canvas, touchIndex: Int, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)
            val isSelected = i == touchIndex && animStrength > 0f

            paint.typeface = typeface
            if (isSelected) {
                paint.textSize = baseTextSize * (1f + highlightIntensity * animStrength)
                paint.alpha = 255
                paint.isFakeBoldText = true
            } else {
                paint.textSize = baseTextSize
                paint.alpha = if (touchIndex >= 0 && animStrength > 0f) {
                    max(100, (255 * (1f - animStrength * highlightIntensity)).toInt())
                } else 255
                paint.isFakeBoldText = false
            }

            val y = centerY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], baseX, y, paint)
        }
        paint.isFakeBoldText = false
    }

    private fun drawFade(canvas: Canvas, touchIndex: Int, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)

            paint.textSize = baseTextSize
            paint.typeface = typeface

            if (touchIndex >= 0 && animStrength > 0f) {
                val dist = abs(i - touchIndex)
                val maxDist = items.size / 2f * fadeRadiusFactor
                val fadeRatio = (dist / maxDist).coerceIn(0f, 1f)
                paint.alpha = max(40, (255 * (1f - fadeRatio * animStrength)).toInt())
            } else {
                paint.alpha = 255
            }

            val y = centerY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], baseX, y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (items.isEmpty()) return super.onTouchEvent(event)

        val availableHeight = (height - paddingTop - paddingBottom).toFloat()
        val relativeY = event.y - paddingTop
        val index = (relativeY / availableHeight * items.size).toInt()
            .coerceIn(0, items.size - 1)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouching = true
                touchY = event.y
                animateIn()
                onLetterSelected?.invoke(items[index])
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                touchY = event.y
                invalidate()
                onLetterSelected?.invoke(items[index])
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onLetterSelected?.invoke(items[index])
                isTouching = false
                animateOut()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animateIn() {
        animator.cancel()
        animator.setFloatValues(animStrength, 1f)
        animator.start()
    }

    private fun animateOut() {
        animator.cancel()
        animator.setFloatValues(animStrength, 0f)
        animator.start()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
