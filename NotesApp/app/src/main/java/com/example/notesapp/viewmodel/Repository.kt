package com.example.notesapp.viewmodel

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notesapp.model.Note
import com.example.notesapp.model.Bank
import com.example.notesapp.room.DateTime
import com.example.notesapp.room.NoteDao
import com.example.notesapp.room.NoteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.Date



interface CbrService {
    @GET("/daily_json.js")
    suspend fun getDaily(): Response<Bank>
    companion object {
        fun getInstance(): CbrService {
            return Retrofit.Builder()
                .baseUrl("https://www.cbr-xml-daily.ru")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CbrService::class.java)
        }
    }
}

@Database(entities = [Note::class], version = 1)
@TypeConverters(DateTime::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}
class Repository(private val db: NoteDatabase) {

    fun getDaily(): Flow<Bank?> {
        return flow {
            CbrService.getInstance().getDaily().body()
        }
    }

    fun getTransactionForDate(date: Date): Flow<List<Note>> {
        return db.noteDao.getTransactionForDate(date)
    }

    fun getTotalIncomeAmount(date: Date): Flow<com.example.notesapp.model.Price> {
        return db.noteDao.getTotalIncomeAmount(date)
    }

    fun getTotalExpenseAmount(date: Date): Flow<com.example.notesapp.model.Price> {
        return db.noteDao.getTotalExpenseAmount(date)
    }

    suspend fun upsert(note: Note) {
        db.noteDao.upsert(note)
    }

    suspend fun delete(note: Note) {
        db.noteDao.delete(note)
    }
}