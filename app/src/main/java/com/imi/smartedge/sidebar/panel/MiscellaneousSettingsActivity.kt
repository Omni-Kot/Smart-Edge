package com.imi.smartedge.sidebar.panel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.imi.smartedge.sidebar.panel.databinding.ActivitySettingsMiscBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MiscellaneousSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsMiscBinding
    private lateinit var panelPrefs: PanelPreferences

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    private val exportFilePicker = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val stream = requireNotNull(contentResolver.openOutputStream(uri, "wt"))
                    stream.use { it.write(panelPrefs.exportToJson().toByteArray(Charsets.UTF_8)) }
                }.isSuccess
            }
            binding.root.showModernToast(getString(if (success) R.string.backup_saved_document else R.string.ui_export_failed_could_not_create_file))
        }
    }

    private val importFilePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) lifecycleScope.launch {
            val oldLanguage = panelPrefs.appLanguage
            val success = withContext(Dispatchers.IO) {
                runCatching {
                    val bytes = requireNotNull(contentResolver.openInputStream(uri)).use { stream ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = stream.read(buffer)
                            if (count < 0) break
                            require(output.size() + count <= SettingsBackup.MAX_BYTES)
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                    panelPrefs.importFromJson(bytes.toString(Charsets.UTF_8))
                }.getOrDefault(false)
            }
            if (success) {
                applyAppTheme(this@MiscellaneousSettingsActivity)
                if (oldLanguage != panelPrefs.appLanguage) restartLocalizedViews() else applyGlobalRefresh()
                binding.root.showModernToast(getString(R.string.ui_settings_imported_successfully))
            } else binding.root.showModernToast(getString(R.string.ui_invalid_backup_file_import_failed))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMiscBinding.inflate(layoutInflater)
        setContentView(binding.root)

        panelPrefs = PanelPreferences(this)

        binding.toolbar.setNavigationOnClickListener { finish() }

        updateLanguageLabel()
        binding.featureLanguage.setOnClickListener { showLanguagePicker() }

        binding.btnExportSettings.setOnClickListener {
            exportFilePicker.launch("smartedge_backup_${System.currentTimeMillis()}.json")
        }

        binding.btnImportSettings.setOnClickListener {
            importFilePicker.launch("application/json")
        }
    }

    private fun updateLanguageLabel() {
        binding.tvLanguageValue.text = when (panelPrefs.appLanguage) {
            "system" -> getString(R.string.language_system)
            "ru" -> getString(R.string.language_russian)
            "es" -> getString(R.string.language_spanish)
            else -> getString(R.string.language_english)
        }
    }

    private fun showLanguagePicker() {
        val languages = arrayOf(getString(R.string.language_system), getString(R.string.language_english), getString(R.string.language_spanish), getString(R.string.language_russian))
        val codes = arrayOf("system", "en", "es", "ru")
        val currentIndex = codes.indexOf(panelPrefs.appLanguage).let { if (it == -1) 0 else it }

        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.ui_choose_app_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selected = codes[which]
                if (selected != panelPrefs.appLanguage) {
                    panelPrefs.appLanguage = selected
                    restartLocalizedViews()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.ui_cancel), null)
            .show()
    }

    private fun restartLocalizedViews() {
        if (FloatingPanelService.isRunning) {
            stopService(Intent(this, FloatingPanelService::class.java))
            if (PanelCapabilities.canStart(this)) androidx.core.content.ContextCompat.startForegroundService(this, Intent(this, FloatingPanelService::class.java))
        }
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun applyGlobalRefresh() {
        val intent = Intent(this, FloatingPanelService::class.java).apply {
            action = FloatingPanelService.ACTION_REFRESH
        }
        if (FloatingPanelService.isRunning) startService(intent)
    }
}
