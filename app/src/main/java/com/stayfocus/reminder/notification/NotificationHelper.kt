package com.stayfocus.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.stayfocus.reminder.MainActivity
import com.stayfocus.reminder.R
import com.stayfocus.reminder.data.model.TodoItem
import com.stayfocus.reminder.data.preferences.UserPreferences

object NotificationHelper {

    const val CHANNEL_ID = "focus_daily_reminder_channel"
    const val NOTIFICATION_ID = 10086
    const val EXTRA_FROM_NOTIFICATION = "extra_from_notification"

    /**
     * 创建高优先级通知渠道
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 发送“常驻不可滑动划掉”的强提醒通知
     * 核心实现：
     * 1. setOngoing(true): 通知栏锁定，滑动无法清除
     * 2. setAutoCancel(false): 点击后不会自动消失
     * 3. 唯有在应用内停留达到设定时间，由代码主动调用 cancelNotification 才会清除
     */
    fun showOngoingReminderNotification(
        context: Context,
        todos: List<TodoItem>,
        dwellDurationSeconds: Int
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 点击通知直接打开 App
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FROM_NOTIFICATION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 组织通知文字内容
        val pendingCount = todos.count { !it.isCompleted }
        val title = if (pendingCount > 0) {
            "📌 今日待办强提醒（共 $pendingCount 项未完成）"
        } else {
            "📌 今日日程确认提醒"
        }

        val contentSummary = if (pendingCount > 0) {
            val firstFew = todos.filter { !it.isCompleted }
                .take(3)
                .mapIndexed { index, item -> "${index + 1}. ${item.title}" }
                .joinToString("\n")
            val more = if (pendingCount > 3) "\n... 还有 ${pendingCount - 3} 项待办" else ""
            val dwellFormatted = when {
                dwellDurationSeconds >= 3600 -> {
                    val h = dwellDurationSeconds / 3600
                    val m = (dwellDurationSeconds % 3600) / 60
                    if (m == 0) "${h} 小时" else "${h} 小时 ${m} 分钟"
                }
                dwellDurationSeconds >= 60 -> "${dwellDurationSeconds / 60} 分钟"
                else -> "${dwellDurationSeconds} 秒"
            }
            "$firstFew$more\n\n🔒 提示：此通知已常驻锁定，需进入 App 停留满 $dwellFormatted 方可自动消除！"
        } else {
            "暂无未完成待办。点击进入 App 停留确认今日计划，常驻通知将自动消除。"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(if (pendingCount > 0) "你有 $pendingCount 项待办等待处理，点击进入应用确认" else "点击进入应用停留确认计划")
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentSummary))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // ⭐ 关键：常驻锁定，不可滑动划掉！
            .setAutoCancel(false) // 点击不关闭
            .setOnlyAlertOnce(false)

        notificationManager.notify(NOTIFICATION_ID, builder.build())

        // 记录通知处于激活状态
        val prefs = UserPreferences(context)
        prefs.setNotificationActive(true)
    }

    /**
     * 消除常驻通知 (当用户在应用内呆满指定时间后调用)
     */
    fun cancelReminderNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        val prefs = UserPreferences(context)
        prefs.setNotificationActive(false)
        prefs.markTodayReviewed()
    }

    /**
     * 检查当前系统通知栏中是否还有该常驻通知
     */
    fun isNotificationCurrentlyActive(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
        } else {
            UserPreferences(context).isNotificationActive()
        }
    }
}
