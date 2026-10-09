package com.turbouro.app.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CrosshairView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var style: String = "CROSS"
        set(value) {
            field = value
            invalidate()
        }

    var crosshairColor: Int = Color.parseColor("#00E5FF")
        set(value) {
            field = value
            paintPrimary.color = value
            invalidate()
        }

    private val paintOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
    }

    private val paintPrimary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeCap = Paint.Cap.ROUND
    }

    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
    }

    private val paintDotOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CC000000")
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        if (cx <= 0 || cy <= 0) return

        paintPrimary.color = crosshairColor
        paintDot.color = crosshairColor

        when (style.uppercase()) {
            "DOT" -> {
                canvas.drawCircle(cx, cy, 4.5f, paintDotOutline)
                canvas.drawCircle(cx, cy, 4f, paintDot)
            }
            "CIRCLE" -> {
                canvas.drawCircle(cx, cy, 14f, paintOutline)
                canvas.drawCircle(cx, cy, 14f, paintPrimary)
                canvas.drawCircle(cx, cy, 2.5f, paintDotOutline)
                canvas.drawCircle(cx, cy, 2.5f, paintDot)
            }
            else -> {
                val armLength = 12f
                val gap = 4f

                canvas.drawLine(cx, cy - gap - armLength, cx, cy - gap, paintOutline)
                canvas.drawLine(cx, cy + gap, cx, cy + gap + armLength, paintOutline)
                canvas.drawLine(cx - gap - armLength, cy, cx - gap, cy, paintOutline)
                canvas.drawLine(cx + gap, cy, cx + gap + armLength, cy, paintOutline)

                canvas.drawLine(cx, cy - gap - armLength, cx, cy - gap, paintPrimary)
                canvas.drawLine(cx, cy + gap, cx, cy + gap + armLength, paintPrimary)
                canvas.drawLine(cx - gap - armLength, cy, cx - gap, cy, paintPrimary)
                canvas.drawLine(cx + gap, cy, cx + gap + armLength, cy, paintPrimary)

                canvas.drawCircle(cx, cy, 1.5f, paintDot)
            }
        }
    }
}
