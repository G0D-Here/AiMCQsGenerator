package com.example.aivideonote.data

data class MCQ(
    val question: String,
    val options: List<String>,
    val answer: String,
    val selectedOption: String? = null
)
