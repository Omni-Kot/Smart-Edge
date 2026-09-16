package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.imi.smartedge.sidebar.panel.databinding.ActivitySettingsAppearanceBinding

/**
 * Handles all UI styling settings:
 * - Theme selection (Origin, HyperOS, etc)
 * - Accent color
 * - Background color & opacity
 * - Corner radius
 * - Scale & Size
 */
class AppearanceSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsAppearanceBinding
    private lateinit var panelPrefs: PanelPreferences

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsAppearanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        panelPrefs = PanelPreferences(this)

        setupToolbar()
        loadCurrentSettings()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadCurrentSettings() {
        binding.sbOpacity.value = (panelPrefs.panelOpacity.toFloat()).coerceIn(binding.sbOpacity.valueFrom, binding.sbOpacity.valueTo)
        binding.tvOpacityValue.text = getString(R.string.value_percent, panelPrefs.panelOpacity)

        binding.sbPanelRadius.value = (panelPrefs.panelCornerRadius.toFloat()).coerceIn(binding.sbPanelRadius.valueFrom, binding.sbPanelRadius.valueTo)
        binding.tvRadiusValue.text = getString(R.string.value_dp, panelPrefs.panelCornerRadius)

        binding.sbIconScale.value = (panelPrefs.scaleFactor).coerceIn(binding.sbIconScale.valueFrom, binding.sbIconScale.valueTo)
        binding.tvIconScaleValue.text = String.format("%.1fx", panelPrefs.scaleFactor)

        binding.sbMaxHeight.value = (panelPrefs.panelMaxHeight.toFloat()).coerceIn(binding.sbMaxHeight.valueFrom, binding.sbMaxHeight.valueTo)
        binding.tvMaxHeightValue.text = getString(R.string.value_dp, panelPrefs.panelMaxHeight)

        binding.sbPickerMaxHeight.value = (panelPrefs.pickerMaxHeight.toFloat()).coerceIn(binding.sbPickerMaxHeight.valueFrom, binding.sbPickerMaxHeight.valueTo)
        binding.tvPickerMaxHeightValue.text = getString(R.string.value_dp, panelPrefs.pickerMaxHeight)

        binding.tvThemeModeValue.text = when (panelPrefs.themeMode) {
            PanelPreferences.MODE_LIGHT -> getString(R.string.ui_light)
            PanelPreferences.MODE_DARK -> getString(R.string.ui_dark)
            else -> getString(R.string.theme_follow_system)
        }

        binding.tvUIStyleValue.text = when (panelPrefs.uiTheme) {
            PanelPreferences.THEME_HYPEROS -> getString(R.string.theme_hyperos)
            PanelPreferences.THEME_REALME -> getString(R.string.theme_realme)
            PanelPreferences.THEME_RICH -> getString(R.string.theme_rich)
            else -> getString(R.string.theme_origin)
        }

        binding.tvIconShapeValue.text = when (panelPrefs.iconShape) {
            PanelPreferences.SHAPE_CIRCLE -> getString(R.string.ui_circle)
            PanelPreferences.SHAPE_SQUARE -> getString(R.string.ui_square)
            PanelPreferences.SHAPE_ROUNDED -> getString(R.string.ui_rounded)
            PanelPreferences.SHAPE_SQUIRCLE -> getString(R.string.ui_squircle)
            else -> getString(R.string.icon_pack_default)
        }

        binding.featureBlur.isChecked = panelPrefs.blurEnabled
        binding.sbBlurAmount.value = (panelPrefs.blurAmount.toFloat()).coerceIn(binding.sbBlurAmount.valueFrom, binding.sbBlurAmount.valueTo)
        binding.tvBlurAmountValue.text = "${panelPrefs.blurAmount}"
        
        binding.featureHideBg.isChecked = panelPrefs.hideBackground
        
        binding.tvColumnsValue.text = resources.getQuantityString(R.plurals.panel_column_count, panelPrefs.panelColumns, panelPrefs.panelColumns)
        
        binding.featureCustomAccent.isChecked = panelPrefs.useCustomAccent
        
        binding.tvCurrentIconPack.text = if (panelPrefs.selectedIconPack == "none") getString(R.string.icon_pack_default) else panelPrefs.iconPackLabel

        binding.btnPickAccent.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(panelPrefs.accentColor))
        binding.btnPickBg.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(panelPrefs.panelBackgroundColor))

        binding.tvHomeButtonStyleValue.text = when (panelPrefs.homeButtonStyle) {
            PanelPreferences.STYLE_POWER -> getString(R.string.button_style_power)
            else -> getString(R.string.button_style_logo)
        }
    }

    private fun setupListeners() {
        binding.sbOpacity.addOnChangeListener { _, value, _ ->
            panelPrefs.panelOpacity = value.toInt()
            binding.tvOpacityValue.text = getString(R.string.value_percent, value.toInt())
            applyOnly()
        }

        binding.sbPanelRadius.addOnChangeListener { _, value, _ ->
            panelPrefs.panelCornerRadius = value.toInt()
            binding.tvRadiusValue.text = getString(R.string.value_dp, value.toInt())
            applyOnly()
        }

        binding.sbIconScale.addOnChangeListener { _, value, _ ->
            panelPrefs.scaleFactor = value
            binding.tvIconScaleValue.text = String.format("%.1fx", value)
            applyOnly()
        }

        binding.sbMaxHeight.addOnChangeListener { _, value, _ ->
            panelPrefs.panelMaxHeight = value.toInt()
            binding.tvMaxHeightValue.text = getString(R.string.value_dp, value.toInt())
            applyOnly()
        }

        binding.sbPickerMaxHeight.addOnChangeListener { _, value, _ ->
            panelPrefs.pickerMaxHeight = value.toInt()
            binding.tvPickerMaxHeightValue.text = getString(R.string.value_dp, value.toInt())
            applyOnly()
        }

        binding.btnResetIconScale.setOnClickListener {
            panelPrefs.scaleFactor = 1.0f
            binding.sbIconScale.value = (1.0f).coerceIn(binding.sbIconScale.valueFrom, binding.sbIconScale.valueTo)
            binding.tvIconScaleValue.text = getString(R.string.value_scale, 1.0f)
            applyOnly()
        }

        binding.btnResetMaxHeight.setOnClickListener {
            val default = 350
            panelPrefs.panelMaxHeight = default
            binding.sbMaxHeight.value = (default.toFloat()).coerceIn(binding.sbMaxHeight.valueFrom, binding.sbMaxHeight.valueTo)
            binding.tvMaxHeightValue.text = getString(R.string.value_dp, default)
            applyOnly()
        }

        binding.btnResetPickerMaxHeight.setOnClickListener {
            val default = 450
            panelPrefs.pickerMaxHeight = default
            binding.sbPickerMaxHeight.value = (default.toFloat()).coerceIn(binding.sbPickerMaxHeight.valueFrom, binding.sbPickerMaxHeight.valueTo)
            binding.tvPickerMaxHeightValue.text = getString(R.string.value_dp, default)
            applyOnly()
        }

        binding.btnResetOpacity.setOnClickListener {
            val default = 100
            panelPrefs.panelOpacity = default
            binding.sbOpacity.value = (default.toFloat()).coerceIn(binding.sbOpacity.valueFrom, binding.sbOpacity.valueTo)
            binding.tvOpacityValue.text = getString(R.string.value_percent, default)
            applyOnly()
        }

        binding.btnResetRadius.setOnClickListener {
            val default = 20
            panelPrefs.panelCornerRadius = default
            binding.sbPanelRadius.value = (default.toFloat()).coerceIn(binding.sbPanelRadius.valueFrom, binding.sbPanelRadius.valueTo)
            binding.tvRadiusValue.text = getString(R.string.value_dp, default)
            applyOnly()
        }

        binding.featureThemeMode.setOnClickListener {
            val options = arrayOf(getString(R.string.theme_follow_system), getString(R.string.ui_light), getString(R.string.ui_dark))
            val values = arrayOf(
                PanelPreferences.MODE_SYSTEM,
                PanelPreferences.MODE_LIGHT,
                PanelPreferences.MODE_DARK
            )
            
            val selectedIndex = values.indexOf(panelPrefs.themeMode).let { if (it == -1) 0 else it }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.app_theme))
                .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                    panelPrefs.themeMode = values[which]
                    binding.tvThemeModeValue.text = options[which]
                    applyAppTheme(this)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.ui_cancel), null)
                .show()
        }

        binding.layoutUIStyle.setOnClickListener {
            val options = arrayOf(getString(R.string.theme_origin), getString(R.string.theme_hyperos), getString(R.string.theme_realme), getString(R.string.theme_rich))
            val values = arrayOf(
                PanelPreferences.THEME_ORIGIN,
                PanelPreferences.THEME_HYPEROS,
                PanelPreferences.THEME_REALME,
                PanelPreferences.THEME_RICH
            )
            
            val selectedIndex = values.indexOf(panelPrefs.uiTheme).let { if (it == -1) 0 else it }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.ui_panel_ui_style))
                .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                    panelPrefs.uiTheme = values[which]
                    binding.tvUIStyleValue.text = options[which]
                    applyOnly()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.ui_cancel), null)
                .show()
        }

        binding.featureIconShape.setOnClickListener {
            val options = arrayOf(getString(R.string.icon_pack_default), getString(R.string.ui_circle), getString(R.string.ui_square), getString(R.string.ui_rounded), getString(R.string.ui_squircle))
            val values = arrayOf(
                PanelPreferences.SHAPE_SYSTEM,
                PanelPreferences.SHAPE_CIRCLE,
                PanelPreferences.SHAPE_SQUARE,
                PanelPreferences.SHAPE_ROUNDED,
                PanelPreferences.SHAPE_SQUIRCLE
            )

            val selectedIndex = values.indexOf(panelPrefs.iconShape).let { if (it == -1) 0 else it }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.icon_shape))
                .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                    panelPrefs.iconShape = values[which]
                    binding.tvIconShapeValue.text = options[which]
                    applyOnly()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.ui_cancel), null)
                .show()
        }

        binding.featureBlur.setOnCheckedChangeListener { _, isChecked ->
            panelPrefs.blurEnabled = isChecked
            applyOnly()
        }

        binding.sbBlurAmount.addOnChangeListener { _, value, _ ->
            panelPrefs.blurAmount = value.toInt()
            binding.tvBlurAmountValue.text = "${value.toInt()}"
            applyOnly()
        }

        binding.featureHideBg.setOnCheckedChangeListener { _, isChecked ->
            panelPrefs.hideBackground = isChecked
            applyOnly()
        }

        binding.featureColumns.setOnClickListener {
            val options = (1..2).map { resources.getQuantityString(R.plurals.panel_column_count, it, it) }.toTypedArray()
            val currentSelectedIndex = (panelPrefs.panelColumns - 1).coerceIn(0, 1)
            var newlySelectedIndex = currentSelectedIndex

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.panel_columns))
                .setSingleChoiceItems(options, currentSelectedIndex) { _, which ->
                    newlySelectedIndex = which
                }
                .setPositiveButton(getString(R.string.ui_apply)) { _, _ ->
                    val columns = newlySelectedIndex + 1
                    panelPrefs.panelColumns = columns
                    binding.tvColumnsValue.text = options[newlySelectedIndex]
                    applyOnly()
                }
                .setNegativeButton(getString(R.string.ui_cancel), null)
                .show()
        }

        binding.featureCustomAccent.setOnCheckedChangeListener { _, isChecked ->
            panelPrefs.useCustomAccent = isChecked
            applyOnly()
        }

        binding.btnSelectIconPack.setOnClickListener {
            IconPackPickerDialog.show(this) {
                binding.tvCurrentIconPack.text = if (panelPrefs.selectedIconPack == "none") getString(R.string.icon_pack_default) else panelPrefs.iconPackLabel
            }
        }

        binding.btnResetUIColors.setOnClickListener {
            panelPrefs.resetUIColors()
            loadCurrentSettings()
            applyOnly()
            binding.root.showModernToast(getString(R.string.ui_ui_colors_restored_to_default))
        }

        binding.btnPickAccent.setOnClickListener {
            if (panelPrefs.uiTheme == PanelPreferences.THEME_ORIGIN) {
                binding.root.showModernToast(getString(R.string.ui_accent_color_is_locked_for_originos_theme))
                return@setOnClickListener
            }
            openColorPicker(Color.parseColor(panelPrefs.accentColor)) { newColor ->
                val hex = String.format("#%06X", (0xFFFFFF and newColor))
                panelPrefs.accentColor = hex
                loadCurrentSettings()
                applyOnly()
            }
        }

        binding.btnPickBg.setOnClickListener {
            if (panelPrefs.uiTheme == PanelPreferences.THEME_ORIGIN) {
                binding.root.showModernToast(getString(R.string.ui_background_color_is_locked_for_originos_theme))
                return@setOnClickListener
            }
            openColorPicker(Color.parseColor(panelPrefs.panelBackgroundColor)) { newColor ->
                val hex = String.format("#E6%06X", (0xFFFFFF and newColor))
                panelPrefs.panelBackgroundColor = hex
                loadCurrentSettings()
                applyOnly()
            }
        }

        binding.featureHomeButton.setOnClickListener {
            val options = arrayOf(getString(R.string.button_style_power), getString(R.string.button_style_logo))
            val values = arrayOf(PanelPreferences.STYLE_POWER, PanelPreferences.STYLE_CLASSIC)
            val selectedIndex = values.indexOf(panelPrefs.homeButtonStyle).let { if (it == -1) 0 else it }

            com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.service_button_style))
                .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                    panelPrefs.homeButtonStyle = values[which]
                    binding.tvHomeButtonStyleValue.text = options[which]
                    applyOnly()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.ui_cancel), null)
                .show()
        }
    }

    private fun applyOnly() {
        val intent = Intent(this, FloatingPanelService::class.java).apply {
            action = FloatingPanelService.ACTION_REFRESH
        }
        startService(intent)
    }
}
