package com.stayfocus.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 待办事项数据实体
 */
@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 0: 普通, 1: 关注, 2: 紧急重要
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
