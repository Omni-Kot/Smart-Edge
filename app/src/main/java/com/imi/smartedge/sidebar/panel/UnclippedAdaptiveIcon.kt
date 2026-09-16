package com.imi.smartedge.sidebar.panel

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable

/**
 * A wrapper for AdaptiveIconDrawable that bypasses its internal mask clipping.
 * It manually draws the background and foreground layers at 150% size.
 */
class UnclippedAdaptiveIcon(private val adaptive: AdaptiveIconDrawable) : Drawable(), Drawable.Callback {

    init {
        // Forward animations to this wrapper
        adaptive.callback = this
        adaptive.background?.callback = this
        adaptive.foreground?.callback = this
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        
        // Calculate the 150% bounds for the layers
        val width = bounds.width()
        val height = bounds.height()
        val extraX = width * 0.25f
        val extraY = height * 0.25f
        
        val layerBounds = Rect(
            (bounds.left - extraX).toInt(),
            (bounds.top - extraY).toInt(),
            (bounds.right + extraX).toInt(),
            (bounds.bottom + extraY).toInt()
        )
        
        // Draw layers directly to bypass AdaptiveIconDrawable's internal mask
        adaptive.background?.let {
            it.bounds = layerBounds
            it.draw(canvas)
        }
        adaptive.foreground?.let {
            it.bounds = layerBounds
            it.draw(canvas)
        }
        
        canvas.restoreToCount(saveCount)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        adaptive.bounds = bounds
    }

    override fun isStateful(): Boolean = adaptive.isStateful

    override fun onStateChange(state: IntArray): Boolean {
        var changed = adaptive.setState(state)
        changed = changed or (adaptive.background?.setState(state) ?: false)
        changed = changed or (adaptive.foreground?.setState(state) ?: false)
        return changed
    }

    override fun onLevelChange(level: Int): Boolean {
        var changed = adaptive.setLevel(level)
        changed = changed or (adaptive.background?.setLevel(level) ?: false)
        changed = changed or (adaptive.foreground?.setLevel(level) ?: false)
        return changed
    }

    override fun setAlpha(alpha: Int) {
        adaptive.alpha = alpha
        adaptive.background?.alpha = alpha
        adaptive.foreground?.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        adaptive.colorFilter = colorFilter
        adaptive.background?.colorFilter = colorFilter
        adaptive.foreground?.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = adaptive.intrinsicWidth
    override fun getIntrinsicHeight(): Int = adaptive.intrinsicHeight

    override fun mutate(): Drawable {
        adaptive.mutate()
        return this
    }

    override fun invalidateDrawable(who: Drawable) {
        invalidateSelf()
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        scheduleSelf(what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        unscheduleSelf(what)
    }

    override fun getConstantState(): ConstantState? {
        return adaptive.constantState?.let { UnclippedConstantState(it) }
    }

    private class UnclippedConstantState(private val base: ConstantState) : ConstantState() {
        override fun newDrawable(): Drawable {
            return UnclippedAdaptiveIcon(base.newDrawable() as AdaptiveIconDrawable)
        }
        override fun getChangingConfigurations(): Int = base.changingConfigurations
    }
}
