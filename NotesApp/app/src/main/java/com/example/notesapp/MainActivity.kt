package com.example.notesapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import kotlin.math.absoluteValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.notes.ui.theme.Purple80
import com.example.notes.ui.theme.PurpleGrey80
import com.example.notesapp.model.Note
import com.example.notesapp.model.Bank
import com.example.notesapp.room.NoteDatabase
import com.example.notesapp.theme.NotesAppTheme
import com.example.notesapp.viewmodel.MainViewModel
import com.example.notesapp.viewmodel.Repository
import com.google.android.engage.common.datamodel.Price
import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Date



class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "main.db"
        ).build()
    }


    private val viewModel by viewModels<MainViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(Repository(database)) as T
                }
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {
                val controller = rememberNavController()

                NavHost(
                    navController = controller,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(viewModel) { note: Note? ->
                            val json = Gson().toJson(note)
                            controller.navigate("addnew/$json")
                        }
                    }
                    composable("addnew/{note}") {
                        AddnewScreen(
                            viewModel,
                            Gson().fromJson(
                                it.arguments?.getString("note"),
                                Note::class.java
                            )
                        ) {
                            controller.popBackStack()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MainScreen(viewModel: MainViewModel, addnew: (Note?) -> Unit) {

    val displayState = viewModel.displayState.collectAsState().value
    val transactions = displayState.transactions.collectAsState(listOf())
    val totalIncome = displayState.totalIncome.collectAsState(com.example.notesapp.model.Price())
    val totalExpense = displayState.totalExpense.collectAsState(com.example.notesapp.model.Price())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { addnew(null) }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Spacer(Modifier.width(16.dp))
                for (period in MainViewModel.Period.entries) {
                    AppButton(
                        text = period.getDisplayName(),
                        onClick = { viewModel.setPeriod(period) }
                    )
                    Spacer(Modifier.width(16.dp))
                }
            }

            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                TotalItem(true, totalIncome.value.value)
                Spacer(Modifier.width(64.dp))
                TotalItem(false, totalExpense.value.value)
            }

            Row {
                val valute by viewModel.value.collectAsState()
                val valuteCodes = listOf("USD", "EUR", "GBP")
                for (code in valuteCodes) {
                    valute[code]?.let {
                        Text(
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(
                                    start = 10.dp,
                                    top = 10.dp,
                                )
                                .background(
                                    color = Color.Green,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .clip(RoundedCornerShape(2.dp)),
                            text = "$code: ${it.value}",
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(transactions.value) { note ->
                    TransactionItem(note, viewModel, addnew)
                }
            }
        }
    }
}

object Utils {
    fun formatAmount(double: Double): String {
        return DecimalFormat("$0").format(double)
    }
}

@Composable
fun TotalItem(
    isIncome: Boolean,
    value: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val color = if (isIncome) Color.Green else Color.Red
        Text(
            fontSize = 24.sp,
            modifier = Modifier,
            text = if (isIncome) "Income" else "Expense",
            color = Color.Black
        )
        Text(
            fontSize = 32.sp,
            modifier = Modifier,
            text = Utils.formatAmount(value.absoluteValue),
            color = color
        )
    }
}


@Composable
fun TransactionItem(
    note: Note,
    viewModel: MainViewModel,
    edit: (Note?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color = Color.White)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable {
                edit(note)
            }
            .padding(16.dp)
    ) {
        val imageResource = if (note.amount > 0) R.drawable.plus else R.drawable.minus

        Image(
            painter = painterResource(id = imageResource),
            contentDescription = if (note.amount > 0) "Доход" else "Расход",
            modifier = Modifier.size(50.dp)
        )


        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = note.name,
                color = (if (note.amount > 0) Color.Green else Color.Red),
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = Utils.formatAmount(note.amount.absoluteValue),
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp)
                )

        }
    }
}


@Composable
fun  AddnewScreen(viewModel: MainViewModel, note: Note?, onFinished: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            val income = rememberSaveable {
                mutableStateOf((note?.amount ?: 0.0) > 0.0)
            }
            val name = rememberSaveable {
                mutableStateOf(note?.name ?: "")
            }
            val amount = rememberSaveable {
                mutableStateOf((note?.amount ?: 0.0).absoluteValue.toString())
            }
            Switch(
                modifier = Modifier.fillMaxWidth(),
                checked = income.value,
                onCheckedChange = { income.value = it }
            )
            Text(text = "Expense          Income",
                fontSize = 25.sp,
                modifier = Modifier.padding(start = 50.dp)
            )

            Spacer(Modifier.height(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = name.value,
                onValueChange = { name.value = it }
            )
            Spacer(Modifier.height(8.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = amount.value,
                onValueChange = { amount.value = it }
            )

            Spacer(Modifier.weight(1f))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val sign = if (income.value) 1 else -1
                    viewModel.upsert(
                        Note(
                            name.value,
                            sign * amount.value.toDouble(),
                            note?.date ?: Date(),
                            note?.id ?: 0
                        )
                    )
                    onFinished()
                }
            ) {
                if (note == null) {
                    Text("Add")
                } else {
                    Text("Update")
                }
            }

            if (note != null) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.delete(note)
                        onFinished()
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun AppButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .width(110.dp)
            .height(30.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Purple80),
        onClick = onClick
    ) {
        Text(
            fontSize = 12.sp,
            modifier = Modifier.background(color = Purple80),
            text = text,
            color = Color.White
        )
    }
}



