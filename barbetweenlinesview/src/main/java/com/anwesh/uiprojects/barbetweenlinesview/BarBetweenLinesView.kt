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
val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val barSizeFactor : Float = 8.9f
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
val rot : Float = 90f

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
    drawRect(RectF(0f, -(barSize / 2)  * sf2, barSize, (barSize / 2) * sf2), paint)
    restore()
    for (j in 0..1) {
        save()
        translate(0f, -barSize / 2 + barSize * j)
        drawLine(0f, 0f, -(w / 2) * sf1, 0f, paint)
        restore()
    }
    drawLine(0f, 0f, -(w / 2 - barSize) * sf3, 0f, paint)
}

fun Canvas.drawBarBetweenLines(scale : Float, w : Float, h : Float, paint : Paint) {
    save()
    translate(w / 2, h / 2)
    rotate(rot * scale.sinify().divideScale(parts - 1, parts))
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
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

    private val renderer : Renderer = Renderer(this)
    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BBLNode(var i : Int, val state : State = State()) {

        private var next : BBLNode? = null
        private var prev : BBLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BBLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBBLNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BBLNode {
            var curr : BBLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BarBetweenLines(var i : Int) {

        private var curr : BBLNode = BBLNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BarBetweenLinesView) {

        private val animator : Animator = Animator(view)
        private val bbl : BarBetweenLines = BarBetweenLines(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bbl.draw(canvas, paint)
            animator.animate {
                bbl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bbl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BarBetweenLinesView {
            var view : BarBetweenLinesView = BarBetweenLinesView(activity)
            activity.setContentView(view)
            return view
        }
    }
}