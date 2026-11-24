package com.stayfocus.reminder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stayfocus.reminder.data.model.TodoItem
import com.stayfocus.reminder.ui.theme.FocusError
import com.stayfocus.reminder.ui.theme.FocusPrimary
import com.stayfocus.reminder.ui.theme.FocusWarning

@Composable
fun AddEditTodoDialog(
    initialTodo: TodoItem? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, priority: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialTodo?.title ?: "") }
    var description by remember { mutableStateOf(initialTodo?.description ?: "") }
    var priority by remember { mutableIntStateOf(initialTodo?.priority ?: 1) }
    var isTitleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTodo == null) "添加新待办" else "编辑待办事项",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("待办标题 *") },
                    placeholder = { Text("例如：晨读英语30分钟、背单词...") },
                    singleLine = true,
                    isError = isTitleError,
                    supportingText = {
                        if (isTitleError) {
                            Text("标题不能为空", color = FocusError)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("补充说明 (可选)") },
                    placeholder = { Text("例如：课本第45页，做完课后习题") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "优先级设置",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = priority == 0,
                        onClick = { priority = 0 },
                        label = { Text("常规") }
                    )
                    FilterChip(
                        selected = priority == 1,
                        onClick = { priority = 1 },
                        label = { Text("重点") }
                    )
                    FilterChip(
                        selected = priority == 2,
                        onClick = { priority = 2 },
                        label = { Text("紧急重要") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else {
                        onConfirm(title.trim(), description.trim(), priority)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FocusPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (initialTodo == null) "添加" else "保存")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("取消")
            }
        }
    )
}
