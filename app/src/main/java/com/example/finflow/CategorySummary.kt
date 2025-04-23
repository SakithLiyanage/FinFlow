package com.example.finflow

import androidx.annotation.DrawableRes

/**
 * Model class representing a summary of expenses per category
 */
data class CategorySummary(
    val name: String,
    val amount: Double,
    @DrawableRes val iconResId: Int,
    val colorResId: Int
)