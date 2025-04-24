package com.example.finflow.utils

import java.text.SimpleDateFormat
import java.util.*


object DateTimeUtils {

    private const val FIXED_CURRENT_TIME = "2025-04-24 03:47:01"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)


    fun getCurrentUtcDateTime(): String {
        return FIXED_CURRENT_TIME
    }

    fun formatToDisplayDate(date: Date): String {
        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return displayFormat.format(date)
    }


    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}