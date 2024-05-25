package com.example.notesapp.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.notesapp.model.Note
import kotlinx.coroutines.flow.Flow
import com.example.notesapp.model.Price
import java.util.Date

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsert(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM `Note` WHERE date > :date")
    fun getTransactionForDate(date: Date): Flow<List<Note>>

    @Query("SELECT sum(amount) as value FROM `Note` WHERE date > :date AND amount > 0")
    fun getTotalIncomeAmount(date: Date): Flow<Price>

    @Query("SELECT sum(amount) as value FROM `Note` WHERE date > :date AND amount < 0")
    fun getTotalExpenseAmount(date: Date): Flow<Price>
}