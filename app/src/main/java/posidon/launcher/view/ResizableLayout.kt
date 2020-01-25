package posidon.launcher.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import posidon.launcher.LauncherMenu
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.tools.Tools
import kotlin.math.abs

class ResizableLayout(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private var dragHandle: View
    private var crossButton: View
    var stopResizingOnFingerLift = true
    var onResizeListener: OnResizeListener? = null
    val MAX_HEIGHT = Tools.getDisplayHeight(context)

    var resizing = false
        set(value) {
            field = value
            if (field) {
                dragHandle.visibility = VISIBLE
                crossButton.visibility = VISIBLE
            } else {
                dragHandle.visibility = GONE
                crossButton.visibility = GONE
            }
            if (layoutParams != null && layoutParams.height < MIN_HEIGHT * resources.displayMetrics.density) layoutParams.height = (MIN_HEIGHT * resources.displayMetrics.density).toInt()
        }

    override fun addView(child: View, index: Int) {
        super.addView(child, index)
        bringChildToFront(dragHandle)
        bringChildToFront(crossButton)
    }

    interface OnResizeListener {
        fun onUpdate(newHeight: Int)
        fun onStop(newHeight: Int)
        fun onCrossPress()
    }

    companion object {
        private const val MIN_HEIGHT = 96
    }

    init {
        View.inflate(context, R.layout.resizable, this)
        clipChildren = false
        dragHandle = findViewById(R.id.handle)
        dragHandle.visibility = if (resizing) VISIBLE else GONE
        dragHandle.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(Main.accentColor))
        dragHandle.backgroundTintMode = PorterDuff.Mode.MULTIPLY
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val location = intArrayOf(0, 0)
                    getLocationOnScreen(location)
                    if (location[1] + event.rawY >= MIN_HEIGHT * resources.displayMetrics.density && location[1] + event.rawY <= MAX_HEIGHT - 96 * resources.displayMetrics.density) {
                        layoutParams.height = (event.rawY - y).toInt()
                        layoutParams = layoutParams
                        onResizeListener?.onUpdate(layoutParams.height)
                        invalidate()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    longPressHandler.removeCallbacks(onLongPress)
                    if (stopResizingOnFingerLift) resizing = false
                    onResizeListener?.onStop(layoutParams.height)
                    invalidate()
                }
            }
            requestDisallowInterceptTouchEvent(true)
            invalidate()
            true
        }
        crossButton = findViewById(R.id.cross)
        crossButton.setOnClickListener {
            resizing = false
            onResizeListener?.onCrossPress()
        }
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ResizableLayout, 0, 0)
        try { resizing = a.getBoolean(R.styleable.ResizableLayout_resizing, false) } finally { a.recycle() }
    }

    private val longPressHandler = Handler()
    private val onLongPress = Runnable {
        if (!LauncherMenu.isActive) {
            Tools.vibrate(context)
            resizing = true
        }
    }

    var startX = 0f
    var startY = 0f
    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                if (!resizing) longPressHandler.postDelayed(onLongPress, ViewConfiguration.getLongPressTimeout().toLong())
            }
            MotionEvent.ACTION_UP -> {
                if (event.eventTime - event.downTime > ViewConfiguration.getLongPressTimeout())
                    return true
                longPressHandler.removeCallbacks(onLongPress)
            }
            MotionEvent.ACTION_CANCEL -> longPressHandler.removeCallbacks(onLongPress)
            MotionEvent.ACTION_MOVE ->
                if (!isAClick(startX, event.x, startY, event.y))
                    longPressHandler.removeCallbacks(onLongPress)
        }
        return super.onInterceptTouchEvent(event)
    }

    fun isAClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        val threshold = 32 * resources.displayMetrics.density
        return abs(startX - endX) < threshold && abs(startY - endY) < threshold
    }
}