package com.stayfocus.reminder.data.repository

import com.stayfocus.reminder.data.local.TodoDao
import com.stayfocus.reminder.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodos()
    val pendingTodos: Flow<List<TodoItem>> = todoDao.getPendingTodos()

    suspend fun getPendingTodosSync(): List<TodoItem> {
        return todoDao.getPendingTodosSync()
    }

    suspend fun insertTodo(todo: TodoItem): Long {
        return todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: TodoItem) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: TodoItem) {
        todoDao.deleteTodo(todo)
    }

    suspend fun toggleCompletion(id: Long, isCompleted: Boolean) {
        todoDao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun deleteCompleted() {
        todoDao.deleteCompletedTodos()
    }
}
