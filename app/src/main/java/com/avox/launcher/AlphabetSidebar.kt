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
import kotlin.math.roundToInt
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
    private var typeface: Typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    private var textSizeSp = 14f

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
            postInvalidateOnAnimation()
        }
    }

    fun setItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        invalidate()
    }

    fun setTypeface(typeface: Typeface?) {
        this.typeface = typeface ?: Typeface.DEFAULT
        invalidate()
    }

    fun setTextSizeSp(sizeSp: Int) {
        textSizeSp = sizeSp.toFloat().coerceAtLeast(8f)
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

    private fun getTouchPosition(availableHeight: Float): Float {
        if (!isTouching && animStrength <= 0f) return -1f
        val relativeY = (touchY - paddingTop).coerceIn(0f, availableHeight)
        return relativeY / availableHeight * items.size - 0.5f
    }

    private fun getWaveInfluence(distance: Float): Float {
        if (distance > waveRadius + 1f) return 0f
        val t = distance / (waveRadius + 1f) * (PI / 2.0)
        return (cos(t) * exp(-5.0 * t * t)).toFloat().coerceAtLeast(0f)
    }

    private fun getHighlightInfluence(distance: Float): Float {
        return (1f - distance.coerceIn(0f, 1f)) * animStrength
    }

    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) return

        val availableHeight = (height - paddingTop - paddingBottom).toFloat()
        val itemHeight = availableHeight / items.size
        @Suppress("DEPRECATION")
        val baseTextSize = textSizeSp * resources.displayMetrics.scaledDensity
        val baseX = width / 2f
        val touchPosition = getTouchPosition(availableHeight)

        when (animStyle) {
            STYLE_WAVE -> drawWave(canvas, touchPosition, baseX, baseTextSize, typeface, itemHeight)
            STYLE_HIGHLIGHT -> drawHighlight(canvas, touchPosition, baseX, baseTextSize, typeface, itemHeight)
            STYLE_FADE -> drawFade(canvas, touchPosition, baseX, baseTextSize, typeface, itemHeight)
            else -> drawWave(canvas, touchPosition, baseX, baseTextSize, typeface, itemHeight)
        }
    }

    private fun drawWave(canvas: Canvas, touchPosition: Float, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)
            var scale = 1f
            var shiftX = 0f
            var localInfluence = 0f

            if (touchPosition >= 0f && animStrength > 0f) {
                val dist = abs(i - touchPosition)
                localInfluence = getWaveInfluence(dist) * animStrength
                if (localInfluence > 0f) {
                    scale = 1f + (maxScaleFactor - 1f) * localInfluence
                    shiftX = -maxShiftPx * localInfluence
                }
            }

            paint.textSize = baseTextSize * scale
            paint.typeface = typeface
            paint.alpha = if (touchPosition >= 0f && animStrength > 0f) {
                val baseAlpha = max(120, (255 * (1f - animStrength * 0.4f)).toInt())
                (baseAlpha + (255 - baseAlpha) * localInfluence).roundToInt().coerceIn(baseAlpha, 255)
            } else 255

            val x = baseX + shiftX
            val y = centerY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], x, y, paint)
        }
    }

    private fun drawHighlight(canvas: Canvas, touchPosition: Float, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)
            val influence = if (touchPosition >= 0f) getHighlightInfluence(abs(i - touchPosition)) else 0f
            val baseAlpha = if (touchPosition >= 0f && animStrength > 0f) {
                max(100, (255 * (1f - animStrength * highlightIntensity)).toInt())
            } else {
                255
            }

            paint.typeface = typeface
            paint.textSize = baseTextSize * (1f + highlightIntensity * influence)
            paint.alpha = (baseAlpha + (255 - baseAlpha) * influence).roundToInt().coerceIn(baseAlpha, 255)
            paint.isFakeBoldText = influence > 0.6f

            val y = centerY - (paint.descent() + paint.ascent()) / 2f
            canvas.drawText(items[i], baseX, y, paint)
        }
        paint.isFakeBoldText = false
    }

    private fun drawFade(canvas: Canvas, touchPosition: Float, baseX: Float, baseTextSize: Float, typeface: Typeface, itemHeight: Float) {
        for (i in items.indices) {
            val centerY = paddingTop + itemHeight * (i + 0.5f)

            paint.textSize = baseTextSize
            paint.typeface = typeface

            if (touchPosition >= 0f && animStrength > 0f) {
                val dist = abs(i - touchPosition)
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
                postInvalidateOnAnimation()
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
        postInvalidateOnAnimation()
    }

    private fun animateOut() {
        animator.cancel()
        animator.setFloatValues(animStrength, 0f)
        animator.start()
        postInvalidateOnAnimation()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
