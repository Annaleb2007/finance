package com.example.notesapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp.model.Note
import com.example.notesapp.model.Bank
import com.example.notesapp.model.Price
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(private val repository: Repository) : ViewModel() {

    val value = MutableStateFlow(HashMap<String, Bank.Valuta>())
    val displayState = MutableStateFlow(DisplayState())

    init {
        setPeriod(Period.LastWeek)

        viewModelScope.launch(Dispatchers.IO) {
            repository.getDaily().collect {
                value.value = it?.valute ?: HashMap()
            }
        }
    }

    data class DisplayState(
        val transactions: Flow<List<Note>> = MutableStateFlow(listOf()),
        val totalIncome: Flow<Price> = MutableStateFlow(Price()),
        val totalExpense: Flow<Price> = MutableStateFlow(Price())
    )

    enum class Period {
        LastWeek,
        LastMonth,
        AllTime;

        fun getDisplayName() = when (this) {
            LastWeek -> "Last week"
            LastMonth -> "Last month"
            AllTime -> "All time"
        }
    }

    fun setPeriod(period: Period) {
        val currentDate = Date().time
        val date = when (period) {
            Period.LastWeek -> Date(currentDate - 7 * 24 * 60 * 60 * 1000L)
            Period.LastMonth -> Date(currentDate - 30 * 24 * 60 * 60 * 1000L)
            Period.AllTime -> Date(0)
        }

        displayState.value = DisplayState(
            repository.getTransactionForDate(date),
            repository.getTotalIncomeAmount(date),
            repository.getTotalExpenseAmount(date)
        )
    }

    fun upsert(note: Note) {
        viewModelScope.launch { repository.upsert(note) }
    }

    fun delete(note: Note) {
        viewModelScope.launch { repository.delete(note) }
    }
}