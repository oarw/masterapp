package com.masterapp.ui.settings

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.masterapp.R
import com.masterapp.databinding.DialogWebdavSettingsBinding
import com.masterapp.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    private val createBackupFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.backupToLocal(requireContext(), uri)
            }
        }
    }

    private val selectBackupFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                showRestoreConfirmationDialog(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = requireContext().getSharedPreferences("app_settings", 0)

        setupThemeToggle()
        setupLanguageSelector()
        setupBackupRestore()
        setupWebdavSettings()
        setupAutoSync()
        setupAbout()
        observeViewModel()
    }

    private fun setupThemeToggle() {
        // 读取当前主题设置
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        // 设置切换事件
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // 确保点击整行都能触发开关
        binding.layoutThemeSetting.setOnClickListener {
            binding.switchDarkMode.isChecked = !binding.switchDarkMode.isChecked
        }
    }

    private fun setupLanguageSelector() {
        // 读取当前语言设置
        val currentLanguage = prefs.getString("language", "zh_CN") ?: "zh_CN"
        updateLanguageDisplay(currentLanguage)

        // 设置点击事件
        binding.layoutLanguageSetting.setOnClickListener {
            val items = arrayOf("简体中文", "English")
            val values = arrayOf("zh_CN", "en_US")
            val checkedItem = values.indexOf(currentLanguage)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.language)
                .setSingleChoiceItems(items, checkedItem) { dialog, which ->
                    val selectedLanguage = values[which]
                    prefs.edit().putString("language", selectedLanguage).apply()
                    updateLanguageDisplay(selectedLanguage)
                    dialog.dismiss()
                    
                    Snackbar.make(binding.root, "语言设置将在应用重启后生效", Snackbar.LENGTH_LONG)
                        .setAction("重启") {
                            // 这里通常会重启应用，但在实际场景中需要适当的实现
                            // 可能需要通过Activity.recreate()或重新启动整个应用
                            Toast.makeText(requireContext(), "请手动重启应用以应用语言更改", Toast.LENGTH_SHORT).show()
                        }
                        .show()
                }
                .show()
        }
    }

    private fun updateLanguageDisplay(langCode: String) {
        binding.textViewLanguage.text = when (langCode) {
            "zh_CN" -> "简体中文"
            "en_US" -> "English"
            else -> "简体中文"
        }
    }

    private fun setupBackupRestore() {
        // 设置备份到本地
        binding.layoutBackupLocal.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
                putExtra(Intent.EXTRA_TITLE, "masterapp_backup_${viewModel.getCurrentDateTimeFormatted()}.zip")
            }
            createBackupFile.launch(intent)
        }

        // 设置从本地恢复
        binding.layoutRestoreLocal.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip"
            }
            selectBackupFile.launch(intent)
        }
    }

    private fun showRestoreConfirmationDialog(uri: android.net.Uri) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认恢复")
            .setMessage("从备份恢复将会覆盖当前的所有数据。这个操作无法撤销，确定要继续吗？")
            .setPositiveButton("确定") { _, _ ->
                viewModel.restoreFromLocal(requireContext(), uri)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupWebdavSettings() {
        // 设置WebDAV配置
        binding.layoutWebdavSettings.setOnClickListener {
            showWebdavSettingsDialog()
        }

        // 设置同步到WebDAV
        binding.layoutSyncToWebdav.setOnClickListener {
            viewModel.syncToWebdav()
        }

        // 设置从WebDAV恢复
        binding.layoutRestoreFromWebdav.setOnClickListener {
            showWebdavRestoreConfirmationDialog()
        }
    }

    private fun showWebdavSettingsDialog() {
        val dialogBinding = DialogWebdavSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.webdav_settings)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val url = dialogBinding.editTextWebdavUrl.text.toString().trim()
                val username = dialogBinding.editTextUsername.text.toString().trim()
                val password = dialogBinding.editTextPassword.text.toString()

                if (url.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.saveWebdavSettings(url, username, password)
                } else {
                    Toast.makeText(requireContext(), "请填写所有必填字段", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .create()

        dialog.show()

        // 测试连接按钮
        dialogBinding.buttonTestConnection.setOnClickListener {
            val url = dialogBinding.editTextWebdavUrl.text.toString().trim()
            val username = dialogBinding.editTextUsername.text.toString().trim()
            val password = dialogBinding.editTextPassword.text.toString()

            if (url.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                viewModel.testWebdavConnection(url, username, password)
            } else {
                Toast.makeText(requireContext(), "请填写所有必填字段", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showWebdavRestoreConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("确认从WebDAV恢复")
            .setMessage("从WebDAV恢复将会覆盖当前的所有数据。这个操作无法撤销，确定要继续吗？")
            .setPositiveButton("确定") { _, _ ->
                viewModel.restoreFromWebdav()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun setupAutoSync() {
        // 初始化自动同步开关状态
        binding.switchAutoSync.isChecked = viewModel.isAutoSyncEnabled()

        // 设置自动同步开关事件
        binding.switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoSync(isChecked)
        }

        // 确保点击整行都能触发开关
        binding.layoutAutoSync.setOnClickListener {
            binding.switchAutoSync.isChecked = !binding.switchAutoSync.isChecked
        }
    }

    private fun setupAbout() {
        // 设置关于应用点击事件
        binding.layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        // 显示当前版本号
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.textViewVersion.text = "v${pInfo.versionName}"
        } catch (e: Exception) {
            binding.textViewVersion.text = "v1.0.0" // 默认版本号
        }
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.about)
            .setMessage("学习规划助手\n\n一款集成日程管理、待办事项、番茄钟、时间管理统计和AI辅助功能的应用，" +
                    "专为个人学习和考研规划设计。\n\n可通过WebDAV进行数据同步并支持本地备份和恢复。")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.webdavStatus.collectLatest { status ->
                when (status) {
                    is WebdavStatus.NotConfigured -> {
                        binding.textViewWebdavStatus.text = "未配置"
                    }
                    is WebdavStatus.Testing -> {
                        binding.textViewWebdavStatus.text = "测试中..."
                    }
                    is WebdavStatus.Configured -> {
                        binding.textViewWebdavStatus.text = "已配置"
                    }
                    is WebdavStatus.ConnectionSuccess -> {
                        binding.textViewWebdavStatus.text = "已连接"
                        Toast.makeText(requireContext(), "连接成功！", Toast.LENGTH_SHORT).show()
                    }
                    is WebdavStatus.ConnectionFailed -> {
                        binding.textViewWebdavStatus.text = "连接失败"
                        Toast.makeText(requireContext(), "连接失败，请检查配置", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.syncStatus.collectLatest { status ->
                when (status) {
                    is SyncStatus.Idle -> {
                        // 空闲状态，不做任何处理
                    }
                    is SyncStatus.Syncing -> {
                        Toast.makeText(requireContext(), R.string.syncing, Toast.LENGTH_SHORT).show()
                    }
                    is SyncStatus.Success -> {
                        Toast.makeText(requireContext(), R.string.sync_success, Toast.LENGTH_SHORT).show()
                    }
                    is SyncStatus.Failed -> {
                        Toast.makeText(requireContext(), status.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.backupStatus.collectLatest { status ->
                when (status) {
                    is BackupStatus.Idle -> {
                        // 空闲状态，不做任何处理
                    }
                    is BackupStatus.Processing -> {
                        Toast.makeText(requireContext(), "处理中...", Toast.LENGTH_SHORT).show()
                    }
                    is BackupStatus.Success -> {
                        Toast.makeText(requireContext(), status.message, Toast.LENGTH_LONG).show()
                    }
                    is BackupStatus.Failed -> {
                        Toast.makeText(requireContext(), status.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}