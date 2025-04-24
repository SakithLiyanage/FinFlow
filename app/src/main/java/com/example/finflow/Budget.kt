package com.example.finflow

data class Budget(
    val id: String,
    val category: String,
    val amount: Double,
    var spent: Double = 0.0
)