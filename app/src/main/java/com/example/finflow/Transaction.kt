package com.example.finflow

import java.util.*

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val date: Date,
    val notes: String? = null
)

/**
 * Enum defining the types of transactions
 */
enum class TransactionType {
    INCOME,
    EXPENSE
}