package com.example.finflow

import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat

class BudgetCategoryAdapter(
    private val categories: List<BudgetCategory>,
    private val currencyFormat: NumberFormat,
    private val onEditClick: (BudgetCategory) -> Unit
) : RecyclerView.Adapter<BudgetCategoryAdapter.ViewHolder>() {

    private val TAG = "BudgetCategoryAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val category = categories[position]
            holder.bind(category)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder at position $position", e)
        }
    }

    override fun getItemCount() = categories.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvBudgetAmount: TextView = itemView.findViewById(R.id.tvBudgetAmount)
        private val tvSpentAmount: TextView = itemView.findViewById(R.id.tvSpentAmount)
        private val tvRemainingAmount: TextView = itemView.findViewById(R.id.tvRemainingAmount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.categoryProgressBar)
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val btnEditCategory: ImageView = itemView.findViewById(R.id.btnEditCategory)

        fun bind(category: BudgetCategory) {
            try {
                tvCategoryName.text = category.name
                tvBudgetAmount.text = currencyFormat.format(category.budget)
                tvSpentAmount.text = "Spent: ${currencyFormat.format(category.spent)}"

                val remaining = category.budget - category.spent
                tvRemainingAmount.text = "Left: ${currencyFormat.format(remaining)}"

                // Set progress bar carefully to avoid crashes
                val progress = if (category.budget > 0) {
                    ((category.spent / category.budget) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                progressBar.progress = progress

                // Set progress bar color based on budget status
                try {
                    val progressColor = when {
                        progress > 90 -> R.color.expense_red
                        progress > 75 -> R.color.warning_orange
                        else -> R.color.primary
                    }
                    progressBar.progressTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, progressColor)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting progress color", e)
                }

                // FIXED: Ensure the correct category icon is assigned and rendered
                try {
                    // Start with safe default
                    var iconResource = R.drawable.ic_category

                    // Map category name to corresponding icon, falling back to default icon
                    when (category.name.lowercase()) {
                        "food" -> iconResource = R.drawable.ic_food
                        "bills" -> iconResource = R.drawable.ic_bills
                        "transport" -> iconResource = R.drawable.ic_transport
                        "shopping" -> iconResource = R.drawable.ic_shopping
                        "entertainment" -> iconResource = R.drawable.ic_entertainment
                        "health" -> iconResource = R.drawable.ic_health
                        "other" -> iconResource = R.drawable.ic_category
                        else -> iconResource = if (category.iconResId != 0) category.iconResId else R.drawable.ic_category
                    }

                    // Set the icon image
                    ivCategoryIcon.setImageResource(iconResource)

                    // Set the icon color
                    val categoryColor = ContextCompat.getColor(
                        itemView.context,
                        category.colorResId.takeIf { it != 0 } ?: R.color.primary
                    )
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(categoryColor)

                } catch (e: Exception) {
                    Log.e(TAG, "Error setting category icon for ${category.name}", e)
                    // Fallback to generic icon
                    ivCategoryIcon.setImageResource(R.drawable.ic_category)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.context, R.color.primary)
                    )
                }

                // Set edit button click listener
                btnEditCategory.setOnClickListener {
                    onEditClick(category)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding category data", e)
            }
        }
    }
}