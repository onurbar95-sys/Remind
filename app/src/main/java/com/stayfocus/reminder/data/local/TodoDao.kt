package com.stayfocus.reminder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stayfocus.reminder.data.model.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC, priority DESC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    fun getPendingTodos(): Flow<List<TodoItem>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, createdAt DESC")
    suspend fun getPendingTodosSync(): List<TodoItem>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: Long): TodoItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItem): Long

    @Update
    suspend fun updateTodo(todo: TodoItem)

    @Delete
    suspend fun deleteTodo(todo: TodoItem)

    @Query("UPDATE todos SET isCompleted = :isCompleted, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos()
}
