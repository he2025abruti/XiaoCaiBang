package com.example.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

class SpinWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val segmentColors = intArrayOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"),
        Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEAA7"),
        Color.parseColor("#DDA0DD"),
        Color.parseColor("#FF8C69"),
        Color.parseColor("#87CEEB")
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var items = listOf<String>()
    private var recipes = listOf<Recipe>()
    private var currentAngle = 0f
    private var isSpinning = false
    private var animator: ValueAnimator? = null

    private val ovalRect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    var onSpinEnd: ((Recipe) -> Unit)? = null
    var onSpinStateChanged: ((Boolean) -> Unit)? = null

    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = true

        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 3f

        pointerPaint.color = Color.parseColor("#FF3333")
        pointerPaint.style = Paint.Style.FILL
    }

    fun setRecipes(recipes: List<Recipe>) {
        this.recipes = recipes
        this.items = recipes.map { it.name }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = min(w, h).toFloat()
        radius = size / 2 * 0.85f
        centerX = w / 2f
        centerY = h / 2f
        ovalRect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        textPaint.textSize = radius * 0.09f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (items.isEmpty()) return

        val count = items.size
        val sweepAngle = 360f / count

        canvas.save()
        canvas.rotate(currentAngle, centerX, centerY)

        for (i in 0 until count) {
            val startAngle = i * sweepAngle

            paint.color = segmentColors[i % segmentColors.size]
            paint.style = Paint.Style.FILL
            canvas.drawArc(ovalRect, startAngle, sweepAngle, true, paint)

            canvas.drawArc(ovalRect, startAngle, sweepAngle, true, borderPaint)

            // Draw text
            canvas.save()
            canvas.rotate(startAngle + sweepAngle / 2, centerX, centerY)

            val textRadius = radius * 0.65f
            val textX = centerX
            val textY = centerY - textRadius

            // Truncate long names
            val displayName = if (items[i].length > 5) {
                items[i].substring(0, 5) + ".."
            } else {
                items[i]
            }

            textPaint.textSize = radius * 0.08f
            canvas.drawText(displayName, textX, textY, textPaint)

            canvas.restore()
        }

        canvas.restore()

        // Draw center circle
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius * 0.12f, paint)

        paint.color = Color.parseColor("#333333")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(centerX, centerY, radius * 0.12f, paint)

        // Draw pointer at top
        val pointerSize = radius * 0.15f
        val pointerY = centerY - radius - pointerSize * 0.3f
        val path = Path()
        path.moveTo(centerX, pointerY + pointerSize * 2)
        path.lineTo(centerX - pointerSize, pointerY)
        path.lineTo(centerX + pointerSize, pointerY)
        path.close()
        canvas.drawPath(path, pointerPaint)
    }

    fun spin() {
        if (isSpinning || items.isEmpty()) return
        isSpinning = true
        onSpinStateChanged?.invoke(true)

        animator?.cancel()

        val startAngle = currentAngle
        val extraRotation = 1800f + (Math.random() * 1800f).toFloat() // 5-10 full rotations
        val endAngle = startAngle + extraRotation

        animator = ValueAnimator.ofFloat(startAngle, endAngle).apply {
            duration = 3500
            interpolator = DecelerateInterpolator(2.5f)
            addUpdateListener { anim ->
                currentAngle = anim.animatedValue as Float
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isSpinning = false
                    onSpinStateChanged?.invoke(false)
                    val selected = getSelectedRecipe()
                    if (selected != null) {
                        onSpinEnd?.invoke(selected)
                    }
                }
            })
            start()
        }
    }

    private fun getSelectedRecipe(): Recipe? {
        if (recipes.isEmpty()) return null
        val count = items.size
        val sweepAngle = 360f / count
        // Pointer is at top = -90 degrees (270 degrees)
        // Normalize the current angle
        val normalizedAngle = ((360f - (currentAngle % 360)) + 270f) % 360f
        val index = (normalizedAngle / sweepAngle).toInt() % count
        return recipes[index]
    }

    fun isSpinning(): Boolean = isSpinning

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (!isSpinning) {
                spin()
            } else {
                // Stop immediately
                animator?.cancel()
                isSpinning = false
                onSpinStateChanged?.invoke(false)
                val selected = getSelectedRecipe()
                if (selected != null) {
                    onSpinEnd?.invoke(selected)
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
