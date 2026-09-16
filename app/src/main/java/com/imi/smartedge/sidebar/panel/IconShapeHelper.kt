package com.imi.smartedge.sidebar.panel

import android.graphics.Outline
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView

/**
 * Utility to apply consistent shapes to icons (Circle, Squircle, Square, etc.)
 */
object IconShapeHelper {

    private val adaptiveProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val minDim = Math.min(view.width, view.height).toFloat()
            if (minDim <= 0) return
            outline.setRoundRect(0, 0, view.width, view.height, minDim * 0.22f)
        }
    }

    private val circleProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val minDim = Math.min(view.width, view.height).toFloat()
            if (minDim <= 0) return
            // Use setRoundRect with exactly 50% radius for a perfect, hardware-aligned circle
            outline.setRoundRect(0, 0, view.width, view.height, minDim / 2f)
        }
    }

    private val squareProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            if (view.width <= 0 || view.height <= 0) return
            val radius = 8 * view.context.resources.displayMetrics.density
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }

    private val roundedProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val minDim = Math.min(view.width, view.height).toFloat()
            if (minDim <= 0) return
            outline.setRoundRect(0, 0, view.width, view.height, minDim * 0.2f)
        }
    }

    private val squircleProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val minDim = Math.min(view.width, view.height).toFloat()
            if (minDim <= 0) return
            outline.setRoundRect(0, 0, view.width, view.height, minDim * 0.35f)
        }
    }

    fun applyShape(view: ImageView, shape: String) {
        val provider = when (shape) {
            PanelPreferences.SHAPE_SYSTEM -> adaptiveProvider
            PanelPreferences.SHAPE_CIRCLE -> circleProvider
            PanelPreferences.SHAPE_SQUARE -> squareProvider
            PanelPreferences.SHAPE_ROUNDED -> roundedProvider
            PanelPreferences.SHAPE_SQUIRCLE -> squircleProvider
            else -> adaptiveProvider
        }

        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.outlineProvider = provider
        view.clipToOutline = true
        
        // Force hardware layer sync
        if (view.width == 0 && view.height == 0) {
            view.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int) {
                    view.removeOnLayoutChangeListener(this)
                    view.invalidateOutline()
                }
            })
        } else {
            view.invalidateOutline()
        }
    }
}
