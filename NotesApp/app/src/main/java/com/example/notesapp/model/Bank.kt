package com.example.notesapp.model

import com.google.gson.annotations.SerializedName

data class Bank(
    @SerializedName("Valute")
    val valute: HashMap<String, Valuta>
) {
    class Valuta(
        @SerializedName("Value")
        val value: String
    )
}
