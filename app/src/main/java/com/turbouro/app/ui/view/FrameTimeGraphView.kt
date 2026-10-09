package com.turbouro.app.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class FrameTimeGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maxSamples = 40
    private val frameTimes = FloatArray(maxSamples)
    private var sampleCount = 0

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#151D2A")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#26354A")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val targetLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2000E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        pathEffect = CornerPathEffect(4f)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val stutterPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF5C67")
        style = Paint.Style.FILL
    }

    private val path = Path()
    private val fillPath = Path()
    private val bgRect = RectF()

    init {
        for (i in 0 until maxSamples) {
            frameTimes[i] = 16.6f
        }
        sampleCount = maxSamples
    }

    fun addSample(frameTimeMs: Float) {
        val safeVal = frameTimeMs.coerceIn(4f, 60f)
        for (i in 0 until maxSamples - 1) {
            frameTimes[i] = frameTimes[i + 1]
        }
        frameTimes[maxSamples - 1] = safeVal
        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bgRect.set(1f, 1f, w.toFloat() - 1f, h.toFloat() - 1f)
        fillPaint.shader = LinearGradient(
            0f, 0f, 0f, h.toFloat(),
            Color.parseColor("#4000E5FF"),
            Color.parseColor("#0000E5FF"),
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        canvas.drawRoundRect(bgRect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(bgRect, 8f, 8f, borderPaint)

        val target60Y = h - (16.6f / 45f) * h
        canvas.drawLine(4f, target60Y, w - 4f, target60Y, targetLinePaint)

        path.reset()
        fillPath.reset()

        val stepX = (w - 8f) / (maxSamples - 1).coerceAtLeast(1)
        var firstX = 4f
        var firstY = h

        for (i in 0 until maxSamples) {
            val x = 4f + i * stepX
            val normalized = (frameTimes[i] / 45f).coerceIn(0.1f, 1f)
            val y = (h - 4f) - normalized * (h - 8f)

            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h - 2f)
                fillPath.lineTo(x, y)
                firstX = x
                firstY = y
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        val lastX = 4f + (maxSamples - 1) * stepX
        fillPath.lineTo(lastX, h - 2f)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)

        for (i in 0 until maxSamples) {
            if (frameTimes[i] > 28f) {
                val x = 4f + i * stepX
                val normalized = (frameTimes[i] / 45f).coerceIn(0.1f, 1f)
                val y = (h - 4f) - normalized * (h - 8f)
                canvas.drawCircle(x, y, 3f, stutterPointPaint)
            }
        }
    }
}
