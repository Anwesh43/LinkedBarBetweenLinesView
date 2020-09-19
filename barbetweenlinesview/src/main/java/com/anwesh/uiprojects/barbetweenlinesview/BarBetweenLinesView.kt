package com.anwesh.uiprojects.barbetweenlinesview

/**
 * Created by anweshmishra on 20/09/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Canvas
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val colors : Array<Int> = arrayOf(
        "#F44336",
        "#FFEB3B",
        "#4CAF50",
        "#2196F3",
        "#FF5722"
).map({Color.parseColor(it)}).toTypedArray()
val parts : Int = 3
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val barSizeFactor : Float = 8.9f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBarBetweenLine(scale : Float, w: Float, h : Float, paint : Paint) {
    val barSize : Float = Math.min(w, h) / barSizeFactor
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    save()
    translate(-w / 2, 0f)
    drawRect(RectF(0f, -(barSize / 2)  * sf1, barSize, (barSize / 2) * sf1), paint)
    restore()
    for (j in 0..1) {
        save()
        translate(0f, -barSize / 2 + barSize * j)
        drawLine(0f, 0f, -(w / 2) * sf2, 0f, paint)
        restore()
    }
    drawLine(0f, 0f, -(w / 2 - barSize) * sf3, 0f, paint)
}

fun Canvas.drawBarBetweenLines(scale : Float, w : Float, h : Float, paint : Paint) {
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        translate(1f - 2 * j, 1f)
        drawBarBetweenLine(scale, w, h, paint)
        restore()
    }
    restore()
}

fun Canvas.drawBBLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawBarBetweenLines(scale, w, h, paint)
}

class BarBetweenLinesView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}