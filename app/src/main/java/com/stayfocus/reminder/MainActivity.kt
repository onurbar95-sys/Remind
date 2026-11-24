package com.stayfocus.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.stayfocus.reminder.notification.NotificationHelper
import com.stayfocus.reminder.ui.screens.MainScreen
import com.stayfocus.reminder.ui.theme.FocusReminderTheme
import com.stayfocus.reminder.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // 申请通知权限回调 (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "通知权限已开启，每日强提醒将准时送达", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "未开启通知权限，将无法在通知栏显示强提醒！", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查并请求通知权限 (Android 13+)
        checkNotificationPermission()

        // 绑定应用生命周期，实现“前台计时、切出暂停”防作弊机制
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onAppForegrounded()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onAppBackgrounded()
                }
                else -> {}
            }
        })

        handleIntent(intent)

        setContent {
            FocusReminderTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_FROM_NOTIFICATION, false) == true) {
            // 从强提醒通知直接点击进入，触发前台停留倒计时
            viewModel.onAppForegrounded()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }
}
