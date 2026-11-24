package com.stayfocus.reminder.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stayfocus.reminder.data.local.AppDatabase
import com.stayfocus.reminder.data.model.TodoItem
import com.stayfocus.reminder.data.preferences.UserPreferences
import com.stayfocus.reminder.data.repository.TodoRepository
import com.stayfocus.reminder.notification.NotificationHelper
import com.stayfocus.reminder.scheduler.ReminderScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TodoRepository
    private val preferences: UserPreferences = UserPreferences(application)

    val todos: StateFlow<List<TodoItem>>
    val dwellDurationSeconds: StateFlow<Int> = preferences.dwellDurationSeconds
    val reminderHour: StateFlow<Int> = preferences.reminderHour
    val reminderMinute: StateFlow<Int> = preferences.reminderMinute
    val isReminderEnabled: StateFlow<Boolean> = preferences.isReminderEnabled
    val isNotificationActive: StateFlow<Boolean> = preferences.isNotificationActive
    val todayReviewed: StateFlow<Boolean> = preferences.todayReviewed

    // 停留倒计时相关状态
    private val _remainingSeconds = MutableStateFlow(preferences.getDwellDuration())
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isCountingDown = MutableStateFlow(false)
    val isCountingDown: StateFlow<Boolean> = _isCountingDown.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _justCompleted = MutableStateFlow(false)
    val justCompleted: StateFlow<Boolean> = _justCompleted.asStateFlow()

    private var countdownJob: Job? = null

    init {
        val todoDao = AppDatabase.getDatabase(application).todoDao()
        repository = TodoRepository(todoDao)
        todos = repository.allTodos.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // 检查通知栏实际状态
        checkActiveNotificationState()
    }

    /**
     * 同步当前系统通知栏常驻通知是否还在
     */
    fun checkActiveNotificationState() {
        val isActive = NotificationHelper.isNotificationCurrentlyActive(getApplication())
        preferences.setNotificationActive(isActive)
        if (isActive && !_isCountingDown.value && !_justCompleted.value) {
            // 如果常驻通知正在展示，且还未完成停留，初始化倒计时
            if (_remainingSeconds.value <= 0 || _remainingSeconds.value > preferences.getDwellDuration()) {
                _remainingSeconds.value = preferences.getDwellDuration()
            }
        }
    }

    /**
     * 当进入前台 (onResume) 时触发
     */
    fun onAppForegrounded() {
        checkActiveNotificationState()
        if (preferences.isNotificationActive()) {
            startOrResumeCountdown()
        }
    }

    /**
     * 当切到后台 (onPause) 时触发：暂停倒计时
     */
    fun onAppBackgrounded() {
        if (_isCountingDown.value) {
            pauseCountdown()
        }
    }

    /**
     * 开始或恢复倒计时
     */
    fun startOrResumeCountdown() {
        if (_isCountingDown.value) return
        _isCountingDown.value = true
        _isPaused.value = false

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            onCountdownFinished()
        }
    }

    /**
     * 暂停倒计时（切出应用时保留剩余秒数）
     */
    fun pauseCountdown() {
        countdownJob?.cancel()
        _isCountingDown.value = false
        _isPaused.value = true
    }

    /**
     * 倒计时结束：消除常驻通知，震动提示，更新状态
     */
    private fun onCountdownFinished() {
        _isCountingDown.value = false
        _isPaused.value = false
        _justCompleted.value = true

        val context = getApplication<Application>()
        // 1. 消除系统常驻通知
        NotificationHelper.cancelReminderNotification(context)

        // 2. 触发触感震动反馈
        triggerVibration(context)

        // 3. 重置倒计时初值
        _remainingSeconds.value = preferences.getDwellDuration()
    }

    private fun triggerVibration(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(300)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun dismissJustCompletedDialog() {
        _justCompleted.value = false
    }

    // --- 待办事项管理 ---
    fun addTodo(title: String, description: String = "", priority: Int = 1) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val todo = TodoItem(
                title = title.trim(),
                description = description.trim(),
                priority = priority
            )
            repository.insertTodo(todo)
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.updateTodo(todo)
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.toggleCompletion(todo.id, !todo.isCompleted)
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
        }
    }

    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.deleteCompleted()
        }
    }

    // --- 设置项 ---
    fun setDwellDuration(seconds: Int) {
        preferences.setDwellDuration(seconds)
        if (!_isCountingDown.value) {
            _remainingSeconds.value = seconds
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        preferences.setReminderTime(hour, minute)
        if (preferences.isReminderEnabled()) {
            ReminderScheduler.scheduleDailyReminder(getApplication(), hour, minute)
        }
    }

    fun setReminderEnabled(enabled: Boolean) {
        preferences.setReminderEnabled(enabled)
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(
                getApplication(),
                preferences.getReminderHour(),
                preferences.getReminderMinute()
            )
        } else {
            ReminderScheduler.cancelDailyReminder(getApplication())
        }
    }

    /**
     * 立即发送测试常驻通知（方便随时体验）
     */
    fun sendTestNotificationNow() {
        viewModelScope.launch {
            val list = repository.getPendingTodosSync()
            val dwell = preferences.getDwellDuration()
            NotificationHelper.showOngoingReminderNotification(getApplication(), list, dwell)
            preferences.setNotificationActive(true)
            preferences.resetTodayReviewed()
            _remainingSeconds.value = dwell
            _justCompleted.value = false
            startOrResumeCountdown()
        }
    }

    /**
     * 手动清除常驻通知 (紧急兜底)
     */
    fun manuallyClearNotification() {
        pauseCountdown()
        NotificationHelper.cancelReminderNotification(getApplication())
        _remainingSeconds.value = preferences.getDwellDuration()
    }
}
