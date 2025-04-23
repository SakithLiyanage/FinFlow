package com.example.finflow

sealed class TransactionListItem {
    data class CategoryHeader(val category: String, val totalAmount: Double) : TransactionListItem()
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
}