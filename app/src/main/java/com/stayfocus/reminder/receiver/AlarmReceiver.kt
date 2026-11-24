package com.stayfocus.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.stayfocus.reminder.data.local.AppDatabase
import com.stayfocus.reminder.data.preferences.UserPreferences
import com.stayfocus.reminder.notification.NotificationHelper
import com.stayfocus.reminder.scheduler.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "接收到定时待办提醒广播: ${intent.action}")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferences(context)
                if (prefs.isReminderEnabled()) {
                    // 查询待办列表
                    val db = AppDatabase.getDatabase(context)
                    val pendingTodos = db.todoDao().getPendingTodosSync()
                    val dwellSeconds = prefs.getDwellDuration()

                    // 发送常驻强提醒通知
                    NotificationHelper.showOngoingReminderNotification(
                        context,
                        pendingTodos,
                        dwellSeconds
                    )

                    // 重新排期明天的同一时间
                    val hour = prefs.getReminderHour()
                    val minute = prefs.getReminderMinute()
                    ReminderScheduler.scheduleDailyReminder(context, hour, minute)
                }
            } catch (e: Exception) {
                Log.e(TAG, "执行定时提醒失败", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val TAG = "AlarmReceiver"
        const val ACTION_DAILY_REMINDER = "com.stayfocus.reminder.ACTION_DAILY_REMINDER"
    }
}
