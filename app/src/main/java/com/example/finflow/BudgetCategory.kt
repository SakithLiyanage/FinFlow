package com.example.finflow

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Model class representing a budget category
 * @property name Name of the budget category (e.g., "Food", "Transport")
 * @property budget The allocated budget amount for this category
 * @property colorResId Resource ID for the category's color
 * @property iconResId Resource ID for the category's icon
 * @property spent The amount already spent in this category (transient property)
 */
data class BudgetCategory(
    val name: String,
    var budget: Double,
    @ColorRes val colorResId: Int,
    @DrawableRes val iconResId: Int,
    var spent: Double = 0.0
)