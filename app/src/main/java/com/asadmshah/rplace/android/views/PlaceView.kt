package com.asadmshah.rplace.android.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import com.asadmshah.rplace.models.DrawEvent
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PlaceView : View {

    companion object {
        private val ScaleFactors = arrayOf(1f, 2f, 5f, 10f, 25f, 50f, 100f, 200f)
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val flingHelper = OverScroller(context)

    private var positionXAnimator: ValueAnimator? = null
    private var positionYAnimator: ValueAnimator? = null
    private var scaleFactorAnimator: ValueAnimator? = null

    private var scaleIndex = 0
    private var scaleFactor = 1f
    private var positionX = 0f
    private var positionY = 0f
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            cancelFling()
            cancelZoom()

            positionX += distanceX
            positionY += distanceY

            if (1024 * scaleFactor < width.toFloat()) {
                positionX = Math.min(Math.max(0f, positionX), 0f)
            } else {
                positionX = Math.min(Math.max(0f, positionX), (1024 * scaleFactor) - width)
            }

            if (1024 * scaleFactor < height.toFloat()) {
                positionY = Math.min(Math.max(0f, positionY), 0f)
            } else {
                positionY = Math.min(Math.max(0f, positionY), (1024 * scaleFactor) - height)
            }

            invalidate()

            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            cancelFling()
            cancelZoom()

            val px = (positionX + e.x) / scaleFactor
            val py = (positionY + e.y) / scaleFactor

            pointFocus(px.toInt(), py.toInt(), (scaleIndex + 1) % ScaleFactors.size, 250)

            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            cancelFling()
            cancelZoom()

            val pointX = (positionX / scaleFactor) + (e.x / scaleFactor)
            val pointY = (positionY / scaleFactor) + (e.y / scaleFactor)

            onPointClickListener?.invoke(pointX.toInt(), pointY.toInt())

            return true
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            cancelFling()
            cancelZoom()

            val startX = positionX.toInt()
            val startY = positionY.toInt()
            val velX = -velocityX.toInt()
            val velY = -velocityY.toInt()
            val minX = 0
            val maxX = if (1024 * scaleFactor < width.toFloat()) {
                0
            } else {
                ((1024 * scaleFactor) - width).toInt()
            }
            val minY = 0
            val maxY = if (1024 * scaleFactor < height.toFloat()) {
                0
            } else {
                ((1024 * scaleFactor) - height).toInt()
            }

            flingHelper.fling(startX, startY, velX, velY, minX, maxX, minY, maxY)

            invalidate()

            return true
        }
    })

    private var srcBitmap: Bitmap? = null
    private var srcCanvas: Canvas? = null
    private val dstMatrix = Matrix()
    private val dstPaint = Paint().apply {
        isAntiAlias = false
        isDither = false
        isFilterBitmap = false
    }

    private var executor: ExecutorService? = null

    var onPointClickListener: ((Int, Int) -> Unit)? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        executor = Executors.newSingleThreadScheduledExecutor()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        executor?.shutdown()
        executor = null

        srcBitmap?.recycle()
        srcBitmap = null
        srcCanvas = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (srcBitmap == null) return

        if (flingHelper.computeScrollOffset()) {
            positionX = flingHelper.currX.toFloat()
            positionY = flingHelper.currY.toFloat()
            invalidate()
        }

        dstMatrix.reset()
        dstMatrix.postScale(scaleFactor, scaleFactor)
        dstMatrix.postTranslate(-positionX, -positionY)

        canvas.drawBitmap(srcBitmap, dstMatrix, dstPaint)
    }

    private fun cancelZoom() {
        positionXAnimator?.removeAllUpdateListeners()
        positionXAnimator?.removeAllListeners()
        positionXAnimator?.cancel()

        positionYAnimator?.removeAllUpdateListeners()
        positionYAnimator?.removeAllListeners()
        positionXAnimator?.cancel()

        scaleFactorAnimator?.removeAllUpdateListeners()
        scaleFactorAnimator?.removeAllListeners()
        scaleFactorAnimator?.cancel()
    }

    private fun cancelFling() {
        if (!flingHelper.isFinished) {
            flingHelper.forceFinished(true)
            invalidate()
        }
    }

    fun setBitmap(bytes: ByteArray) {
        val options = BitmapFactory.Options().apply { inMutable = true }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        setBitmap(bitmap)
    }

    fun setBitmap(bitmap: Bitmap, canvas: Canvas = Canvas(bitmap)) {
        srcBitmap?.recycle()

        srcBitmap = bitmap
        srcCanvas = canvas

        invalidate()
    }

    private val newPaint = Paint(dstPaint)

    fun drawPoints(drawEvents: List<DrawEvent>) {
        executor?.submit {
            drawEvents.forEach {
                newPaint.color = it.color
                srcCanvas?.drawPoint(it.position.x.toFloat(), it.position.y.toFloat(), newPaint)
            }

            postInvalidate()
        }
    }

    fun zoomToLevel(scaleToIndex: Int, animationDuration: Long = 2500) {
        require(scaleToIndex >= 0 && scaleToIndex < ScaleFactors.size)

        val px = if (1024 * scaleFactor < width) {
            (1024 / 2).toFloat()
        } else {
            (positionX + (width / 2)) / scaleFactor
        }

        val py = if (1024 * scaleFactor < height) {
            (1024 / 2).toFloat()
        } else {
            (positionY + (height / 2)) / scaleFactor
        }

        pointFocus(px.toInt(), py.toInt(), scaleToIndex, animationDuration)
    }

    fun pointFocus(x: Int, y: Int, scaleToIndex: Int = ScaleFactors.size - 1, animationDuration: Long = 2500) {
        cancelFling()
        cancelZoom()

        val oldPositionX = positionX
        val oldPositionY = positionY
        val oldScaleFactor = scaleFactor

        positionX = x.toFloat()
        positionY = y.toFloat()

        scaleIndex = scaleToIndex
        scaleFactor = ScaleFactors[scaleIndex]

        positionX *= scaleFactor
        positionY *= scaleFactor

        positionX -= width / 2
        positionY -= height / 2

        if (1024 * scaleFactor < width.toFloat()) {
            positionX = Math.min(Math.max(0f, positionX), 0f)
        } else {
            positionX = Math.min(Math.max(0f, positionX), (1024 * scaleFactor) - width)
        }

        if (1024 * scaleFactor < height.toFloat()) {
            positionY = Math.min(Math.max(0f, positionY), 0f)
        } else {
            positionY = Math.min(Math.max(0f, positionY), (1024 * scaleFactor) - height)
        }

        val newPositionX = positionX
        val newPositionY = positionY
        val newScaleFactor = scaleFactor

        positionX = oldPositionX
        positionY = oldPositionY
        scaleFactor = oldScaleFactor

        positionXAnimator = ValueAnimator.ofFloat(oldPositionX, newPositionX)
        positionXAnimator?.duration = animationDuration
        positionXAnimator?.addUpdateListener {
            positionX = it.animatedValue as Float
            invalidate()
        }
        positionXAnimator?.start()

        positionYAnimator = ValueAnimator.ofFloat(oldPositionY, newPositionY)
        positionYAnimator?.duration = animationDuration
        positionYAnimator?.addUpdateListener {
            positionY = it.animatedValue as Float
            invalidate()
        }
        positionYAnimator?.start()

        scaleFactorAnimator = ValueAnimator.ofFloat(oldScaleFactor, newScaleFactor)
        scaleFactorAnimator?.duration = animationDuration
        scaleFactorAnimator?.addUpdateListener {
            scaleFactor = it.animatedValue as Float
            invalidate()
        }
        scaleFactorAnimator?.start()

        invalidate()
    }

}