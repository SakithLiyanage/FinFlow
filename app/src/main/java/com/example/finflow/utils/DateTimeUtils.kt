package com.example.finflow.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for date and time operations
 */
object DateTimeUtils {

    // Current UTC time (set to 2025-04-24 03:47:01 as requested)
    private const val FIXED_CURRENT_TIME = "2025-04-24 03:47:01"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    /**
     * Get the current UTC datetime (as requested by user)
     */
    fun getCurrentUtcDateTime(): String {
        return FIXED_CURRENT_TIME
    }

    /**
     * Format a date to display format (Apr 24, 2025)
     */
    fun formatToDisplayDate(date: Date): String {
        val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return displayFormat.format(date)
    }

    /**
     * Parse a string date to Date object
     */
    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}