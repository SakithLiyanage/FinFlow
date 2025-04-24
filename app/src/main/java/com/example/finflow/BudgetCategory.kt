package com.example.finflow

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes


data class BudgetCategory(
    val name: String,
    var budget: Double,
    @ColorRes val colorResId: Int,
    @DrawableRes val iconResId: Int,
    var spent: Double = 0.0
)