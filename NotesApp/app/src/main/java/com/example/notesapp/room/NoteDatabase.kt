package com.example.notesapp.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notesapp.model.Note


@Database(entities = [Note::class], version = 1)
@TypeConverters(DateTime::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
}