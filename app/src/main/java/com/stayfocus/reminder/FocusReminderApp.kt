package com.stayfocus.reminder

import android.app.Application
import com.stayfocus.reminder.data.preferences.UserPreferences
import com.stayfocus.reminder.notification.NotificationHelper
import com.stayfocus.reminder.scheduler.ReminderScheduler

class FocusReminderApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // 创建高优先级通知渠道
        NotificationHelper.createNotificationChannel(this)

        // 检查并确保排期有效
        val prefs = UserPreferences(this)
        if (prefs.isReminderEnabled()) {
            ReminderScheduler.scheduleDailyReminder(
                this,
                prefs.getReminderHour(),
                prefs.getReminderMinute()
            )
        }
    }
}
