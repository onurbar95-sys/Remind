package com.stayfocus.reminder.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _dwellDurationSeconds = MutableStateFlow(getDwellDuration())
    val dwellDurationSeconds: StateFlow<Int> = _dwellDurationSeconds.asStateFlow()

    private val _reminderHour = MutableStateFlow(getReminderHour())
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(getReminderMinute())
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    private val _isReminderEnabled = MutableStateFlow(isReminderEnabled())
    val isReminderEnabled: StateFlow<Boolean> = _isReminderEnabled.asStateFlow()

    private val _isNotificationActive = MutableStateFlow(isNotificationActive())
    val isNotificationActive: StateFlow<Boolean> = _isNotificationActive.asStateFlow()

    private val _todayReviewed = MutableStateFlow(isTodayReviewed())
    val todayReviewed: StateFlow<Boolean> = _todayReviewed.asStateFlow()

    private val _isKeepScreenOn = MutableStateFlow(isKeepScreenOn())
    val isKeepScreenOn: StateFlow<Boolean> = _isKeepScreenOn.asStateFlow()

    fun getDwellDuration(): Int {
        return prefs.getInt(KEY_DWELL_SECONDS, DEFAULT_DWELL_SECONDS)
    }

    fun setDwellDuration(seconds: Int) {
        prefs.edit().putInt(KEY_DWELL_SECONDS, seconds).apply()
        _dwellDurationSeconds.value = seconds
    }

    fun getSavedRemainingSeconds(): Int {
        return prefs.getInt(KEY_SAVED_REMAINING_SECONDS, -1)
    }

    fun setSavedRemainingSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_SAVED_REMAINING_SECONDS, seconds).apply()
    }

    fun clearSavedRemainingSeconds() {
        prefs.edit().remove(KEY_SAVED_REMAINING_SECONDS).apply()
    }

    fun isKeepScreenOn(): Boolean {
        return prefs.getBoolean(KEY_KEEP_SCREEN_ON, true)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
        _isKeepScreenOn.value = enabled
    }

    fun getReminderHour(): Int {
        return prefs.getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR)
    }

    fun getReminderMinute(): Int {
        return prefs.getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
        _reminderHour.value = hour
        _reminderMinute.value = minute
    }

    fun isReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
        _isReminderEnabled.value = enabled
    }

    fun isNotificationActive(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_ACTIVE, false)
    }

    fun setNotificationActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ACTIVE, active).apply()
        _isNotificationActive.value = active
    }

    fun isTodayReviewed(): Boolean {
        val todayStr = getTodayDateString()
        return prefs.getString(KEY_LAST_REVIEWED_DATE, "") == todayStr
    }

    fun markTodayReviewed() {
        val todayStr = getTodayDateString()
        prefs.edit().putString(KEY_LAST_REVIEWED_DATE, todayStr).apply()
        clearSavedRemainingSeconds()
        _todayReviewed.value = true
    }

    fun resetTodayReviewed() {
        prefs.edit().remove(KEY_LAST_REVIEWED_DATE).apply()
        clearSavedRemainingSeconds()
        _todayReviewed.value = false
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        private const val PREFS_NAME = "focus_reminder_prefs"
        private const val KEY_DWELL_SECONDS = "dwell_seconds"
        private const val KEY_SAVED_REMAINING_SECONDS = "saved_remaining_seconds"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_NOTIFICATION_ACTIVE = "notification_active"
        private const val KEY_LAST_REVIEWED_DATE = "last_reviewed_date"

        const val DEFAULT_DWELL_SECONDS = 7200 // 默认设定支持长达 2 小时 (7200秒)
        const val DEFAULT_REMINDER_HOUR = 8 // 默认每天早上 8:30
        const val DEFAULT_REMINDER_MINUTE = 30
    }
}
