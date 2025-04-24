package com.example.finflow

import java.util.*

enum class TransactionType {
    INCOME,
    EXPENSE
}

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Date = Date(),
    val notes: String = "",
    val userId: String = "SakithLiyanage",
    val timestamp: String? = null
)