package com.imi.smartedge.sidebar.panel

import org.json.JSONObject

/** Strict, complete backup. Validation finishes before any preference is changed. */
object SettingsBackup {
    const val MAX_BYTES = 1_048_576
    fun encode(values: Map<String, Any>): String = JSONObject(values)
        .put("_app", "SmartEdge").put("_version", 2).toString(2)

    fun decode(json: String, schema: Map<String, Any>): Map<String, Any> {
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_BYTES)
        val obj = JSONObject(json)
        require(obj.opt("_app") == "SmartEdge" && obj.opt("_version") == 2)
        require(obj.keys().asSequence().toSet() == schema.keys + setOf("_app", "_version"))
        return schema.mapValues { (key, expected) ->
            val value = obj.get(key)
            val typed = when (expected) {
                is Boolean -> { require(value is Boolean); value }
                is Int -> { require(value is Int); value }
                is Float -> { require(value is Number); value.toFloat().also { require(it.isFinite()) } }
                is String -> { require(value is String); value }
                else -> error("Unsupported preference type")
            }
            validate(key, typed)
            typed
        }
    }

    private fun validate(key: String, value: Any) {
        if (value is Int) {
            val range = when {
                key.endsWith("_action") -> 0..15
                key == "panel_columns" -> 1..2
                key == "theme_mode" -> 0..2
                key == "handle_offset" -> -500..500
                key == "panel_opacity" -> 10..100
                key == "panel_radius" -> 0..60
                key == "panel_max_height" -> 200..800
                key == "picker_max_height" -> 300..800
                key == "blur_amount" -> 5..50
                key == "pill_width" -> 1..10
                key == "handle_width" -> 5..100
                key == "handle_height" -> 20..400
                key in setOf("slide_sensitivity", "swipe_sensitivity", "freeform_custom_width", "freeform_custom_height") -> 1..100
                key == "picker_gap" -> 0..100
                key == "animation_speed" -> 0..5000
                else -> 0..10000
            }
            require(value in range)
            if (key in setOf("handle_width", "handle_height", "panel_max_height", "picker_max_height", "freeform_custom_width", "freeform_custom_height", "slide_sensitivity", "swipe_sensitivity")) require(value > 0)
        }
        if (value is Float) {
            require(value in 0.8f..2f)
            require(kotlin.math.abs(value * 10 - kotlin.math.round(value * 10)) < 0.0001f)
        }
        if (value is String) {
            require(value.length <= 200_000)
            val choices = when (key) {
                "panel_side" -> setOf("left", "right")
                "ui_theme" -> setOf("origin", "hyperos", "realme", "rich")
                "icon_shape" -> setOf("system", "circle", "squircle", "square", "rounded")
                "home_button_style" -> setOf("power", "classic")
                "freeform_window_mode" -> setOf("standard", "portrait", "maximized", "custom")
                "picker_anim_type" -> setOf("slide", "popup")
                "Locale.Helper.Selected.Language" -> setOf("system", "en", "es", "ru")
                else -> null
            }
            if (choices != null) require(value in choices)
            if (key in setOf("accent_color", "panel_bg_color", "pill_color")) require(Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?").matches(value))
        }
    }
}
