package com.stayfocus.reminder.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stayfocus.reminder.data.model.TodoItem
import com.stayfocus.reminder.ui.components.AddEditTodoDialog
import com.stayfocus.reminder.ui.components.DwellCountdownCard
import com.stayfocus.reminder.ui.components.TodoItemRow
import com.stayfocus.reminder.ui.theme.FocusPrimary
import com.stayfocus.reminder.ui.theme.FocusSuccess
import com.stayfocus.reminder.ui.viewmodel.MainViewModel

@Composable
fun TodoListScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val todos by viewModel.todos.collectAsState()
    val isNotificationActive by viewModel.isNotificationActive.collectAsState()
    val isCountingDown by viewModel.isCountingDown.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalDwellSeconds by viewModel.dwellDurationSeconds.collectAsState()
    val todayReviewed by viewModel.todayReviewed.collectAsState()
    val justCompleted by viewModel.justCompleted.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }

    val pendingList = todos.filter { !it.isCompleted }
    val completedList = todos.filter { it.isCompleted }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. 顶部停留倒计时卡片
            item {
                DwellCountdownCard(
                    isNotificationActive = isNotificationActive,
                    isCountingDown = isCountingDown,
                    isPaused = isPaused,
                    remainingSeconds = remainingSeconds,
                    totalSeconds = totalDwellSeconds,
                    todayReviewed = todayReviewed,
                    onStartTestNotification = { viewModel.sendTestNotificationNow() },
                    onManualClear = { viewModel.manuallyClearNotification() }
                )
            }

            // 2. 待办标题栏
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日待办 (${pendingList.size}项未完成)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 3. 未完成列表
            if (pendingList.isEmpty() && completedList.isEmpty()) {
                item {
                    EmptyTodoPlaceholder(onAddClick = { showAddDialog = true })
                }
            } else {
                items(pendingList, key = { it.id }) { item ->
                    TodoItemRow(
                        todo = item,
                        onToggle = { viewModel.toggleTodo(item) },
                        onEdit = { editingTodo = item },
                        onDelete = { viewModel.deleteTodo(item) }
                    )
                }
            }

            // 4. 已完成列表
            if (completedList.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已完成 (${completedList.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )

                        TextButton(onClick = { viewModel.clearCompletedTodos() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "清空已完成",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                items(completedList, key = { it.id }) { item ->
                    TodoItemRow(
                        todo = item,
                        onToggle = { viewModel.toggleTodo(item) },
                        onEdit = { editingTodo = item },
                        onDelete = { viewModel.deleteTodo(item) }
                    )
                }
            }
        }

        // 浮动添加按钮
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = FocusPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "添加待办")
        }
    }

    // 添加待办弹窗
    if (showAddDialog) {
        AddEditTodoDialog(
            initialTodo = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, priority ->
                viewModel.addTodo(title, desc, priority)
                showAddDialog = false
            }
        )
    }

    // 编辑待办弹窗
    editingTodo?.let { item ->
        AddEditTodoDialog(
            initialTodo = item,
            onDismiss = { editingTodo = null },
            onConfirm = { title, desc, priority ->
                viewModel.updateTodo(
                    item.copy(
                        title = title,
                        description = desc,
                        priority = priority,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                editingTodo = null
            }
        )
    }

    // 停留完成祝贺弹窗
    if (justCompleted) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissJustCompletedDialog() },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = FocusSuccess,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "打卡停留完成！",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = "您已在应用内保持停留满指定时间，今日计划已充分确认！\n\n手机通知栏中的常驻待办提醒已自动为您消除。",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissJustCompletedDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("我知道了")
                }
            }
        )
    }
}

@Composable
fun EmptyTodoPlaceholder(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "今天还没有待办事项",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "添加每天要做的事情，设定时间一到将常驻强提醒！",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("添加第一条待办", fontSize = 13.sp)
        }
    }
}
