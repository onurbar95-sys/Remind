package com.stayfocus.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.stayfocus.reminder.data.preferences.UserPreferences
import com.stayfocus.reminder.scheduler.ReminderScheduler

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "系统重启完成，正在恢复每日强提醒排期...")
            val prefs = UserPreferences(context)
            if (prefs.isReminderEnabled()) {
                val hour = prefs.getReminderHour()
                val minute = prefs.getReminderMinute()
                ReminderScheduler.scheduleDailyReminder(context, hour, minute)
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
