package com.stayfocus.reminder.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stayfocus.reminder.ui.theme.FocusPrimary
import com.stayfocus.reminder.ui.theme.FocusSuccess
import com.stayfocus.reminder.ui.theme.FocusWarning
import com.stayfocus.reminder.ui.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dwellSeconds by viewModel.dwellDurationSeconds.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    val isReminderEnabled by viewModel.isReminderEnabled.collectAsState()
    val isNotificationActive by viewModel.isNotificationActive.collectAsState()
    val isKeepScreenOn by viewModel.isKeepScreenOn.collectAsState()

    var sliderValue by remember(dwellSeconds) { mutableFloatStateOf(dwellSeconds.toFloat()) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. 停留时长调节 (核心需求，支持长达两小时及以上) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = FocusPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "应用内停留消除时长",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "在此时间结束前，通知将锁定在手机通知栏（无法划掉）。只有您进入 App 停留满该时长，通知才会自动消除。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 时长显示与自定义按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "当前设定:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatDwellTime(dwellSeconds),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = FocusPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { showCustomDurationDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("自定义时分")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 连续滑块：支持 1分钟 到 4小时 (14400秒)
                Slider(
                    value = sliderValue.coerceIn(60f, 14400f),
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        val roundedToMinute = ((sliderValue.toInt() + 150) / 300) * 300 // 5分钟整步进
                        val finalSec = roundedToMinute.coerceIn(60, 14400)
                        sliderValue = finalSec.toFloat()
                        viewModel.setDwellDuration(finalSec)
                    },
                    valueRange = 60f..14400f,
                    colors = SliderDefaults.colors(
                        thumbColor = FocusPrimary,
                        activeTrackColor = FocusPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1 分钟", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("1 小时", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    Text("2 小时", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = FocusPrimary)
                    Text("4 小时", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 快捷预设按钮 (包含两小时、一小时等)
                Text(
                    text = "常用快捷选项:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(6.dp))

                val presets = listOf(
                    60 to "1分钟(测试)",
                    300 to "5分钟",
                    900 to "15分钟",
                    1800 to "30分钟",
                    3600 to "1小时",
                    5400 to "1.5小时",
                    7200 to "⭐ 2小时 (两钟)",
                    10800 to "3小时"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (seconds, label) ->
                        FilterChip(
                            selected = dwellSeconds == seconds,
                            onClick = {
                                sliderValue = seconds.toFloat()
                                viewModel.setDwellDuration(seconds)
                            },
                            label = { Text(label, fontWeight = if (seconds == 7200) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }

        // --- 2. 专注屏幕常亮 (特别适合 2 小时长时间停留) ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = FocusWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "保持屏幕常亮",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "应用停留期间防止手机自动息屏锁屏（非常适合 2 小时专注自习、阅读或办公）。",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Switch(
                    checked = isKeepScreenOn,
                    onCheckedChange = { viewModel.setKeepScreenOn(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = FocusPrimary)
                )
            }
        }

        // --- 3. 每日提醒时间与总开关 ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = FocusPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "每日强提醒开关",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { viewModel.setReminderEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = FocusPrimary)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "提醒发送时间",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "每天在此时间准时发送常驻通知",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            showTimePicker(context, reminderHour, reminderMinute) { h, m ->
                                viewModel.setReminderTime(h, m)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format("%02d:%02d", reminderHour, reminderMinute),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 4. 实时测试与调试 ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "效果体验与测试",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "点击下方按钮立即弹出常驻不可划掉的通知，下拉通知栏尝试滑动清除，体验停留消除流程。",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.sendTestNotificationNow() },
                        colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("立即发送通知")
                    }

                    OutlinedButton(
                        onClick = { viewModel.manuallyClearNotification() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("手动清除通知")
                    }
                }
            }
        }

        // --- 5. 安卓保活与后台优化说明 ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "手机系统权限建议 (确保每日准时提醒)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "1. 通知权限：请允许本应用发送通知并设为“高优先级/横幅提醒”\n" +
                            "2. 电池策略：将本应用电池优化设为“无限制/允许后台活动”\n" +
                            "3. 2小时断点续计：即使中途临时切出或手机重启，应用会自动保存剩余时长，再次进入继续倒计时。",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // 自定义精准时分对话框
    if (showCustomDurationDialog) {
        CustomDurationDialog(
            initialSeconds = dwellSeconds,
            onDismiss = { showCustomDurationDialog = false },
            onConfirm = { newSeconds ->
                sliderValue = newSeconds.toFloat()
                viewModel.setDwellDuration(newSeconds)
                showCustomDurationDialog = false
            }
        )
    }
}

@Composable
fun CustomDurationDialog(
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (seconds: Int) -> Unit
) {
    var hours by remember { mutableIntStateOf(initialSeconds / 3600) }
    var minutes by remember { mutableIntStateOf((initialSeconds % 3600) / 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设定精确停留时长", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "请设定进入应用必须停留的时长：",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时调节
                    NumberStepper(
                        label = "小时",
                        value = hours,
                        range = 0..12,
                        onValueChange = { hours = it }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    // 分钟调节
                    NumberStepper(
                        label = "分钟",
                        value = minutes,
                        range = 0..59,
                        onValueChange = { minutes = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val total = hours * 3600 + minutes * 60
                Text(
                    text = "折合总时长: ${if (total > 0) formatDwellTime(total) else "至少需选择1分钟"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FocusPrimary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val totalSec = hours * 3600 + minutes * 60
                    if (totalSec >= 60) {
                        onConfirm(totalSec)
                    }
                },
                enabled = (hours * 3600 + minutes * 60) >= 60,
                colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("取消")
            }
        }
    )
}

@Composable
fun NumberStepper(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = { if (value > range.first) onValueChange(value - 1) },
                enabled = value > range.first,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "减少", modifier = Modifier.size(16.dp))
            }

            Text(
                text = String.format("%02d", value),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            IconButton(
                onClick = { if (value < range.last) onValueChange(value + 1) },
                enabled = value < range.last,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "增加", modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatDwellTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return buildString {
        if (h > 0) append("${h} 小时 ")
        if (m > 0) append("${m} 分钟 ")
        if (s > 0 || (h == 0 && m == 0)) append("${s} 秒")
    }.trim()
}

private fun showTimePicker(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onTimeSet: (hour: Int, minute: Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSet(hourOfDay, minute)
        },
        initialHour,
        initialMinute,
        true // 24小时制
    ).show()
}
