package com.example.finflow

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class BudgetsAdapter(
    private var budgets: ArrayList<Budget>,
    private val onEditClick: (Budget) -> Unit,
    private val onDeleteClick: (Budget) -> Unit
) : RecyclerView.Adapter<BudgetsAdapter.BudgetViewHolder>() {

    private val categoryIconMap = mapOf(
        "Food" to R.drawable.ic_food,
        "Transport" to R.drawable.ic_transport,
        "Shopping" to R.drawable.ic_shopping,
        "Bills" to R.drawable.ic_bills,
        "Health" to R.drawable.ic_health,
        "Education" to R.drawable.ic_education,
        "Entertainment" to R.drawable.ic_entertainment,
        "Other" to R.drawable.ic_other
    )

    private val categoryColorMap = mapOf(
        "Food" to "#FF5722",
        "Transport" to "#FF9800",
        "Shopping" to "#F44336",
        "Bills" to "#E91E63",
        "Health" to "#9C27B0",
        "Education" to "#673AB7",
        "Entertainment" to "#3F51B5",
        "Other" to "#607D8B"
    )

    class BudgetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvBudgetCategory: TextView = itemView.findViewById(R.id.tvBudgetCategory)
        val tvBudgetSpent: TextView = itemView.findViewById(R.id.tvBudgetSpent)
        val tvBudgetLimit: TextView = itemView.findViewById(R.id.tvBudgetLimit)
        val tvBudgetPercentage: TextView = itemView.findViewById(R.id.tvBudgetPercentage)
        val categoryBudgetProgress: LinearProgressIndicator = itemView.findViewById(R.id.categoryBudgetProgress)
        val btnEditBudget: ImageView = itemView.findViewById(R.id.btnEditBudget)
        val btnDeleteBudget: ImageView = itemView.findViewById(R.id.btnDeleteBudget)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_budget, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val budget = budgets[position]
        val context = holder.itemView.context

        holder.tvBudgetCategory.text = budget.category

        val iconResId = categoryIconMap[budget.category] ?: R.drawable.ic_other
        holder.ivCategoryIcon.setImageResource(iconResId)

        val categoryColor = categoryColorMap[budget.category] ?: "#607D8B"
        holder.ivCategoryIcon.imageTintList = ColorStateList.valueOf(Color.parseColor(categoryColor))

        holder.tvBudgetSpent.text = String.format("LKR %.2f", budget.spent)
        holder.tvBudgetLimit.text = String.format("LKR %.2f", budget.amount)

        val percentage = if (budget.amount > 0) (budget.spent / budget.amount * 100).toInt() else 0
        holder.tvBudgetPercentage.text = "$percentage%"
        holder.categoryBudgetProgress.progress = percentage

        val progressColor = when {
            percentage < 50 -> Color.parseColor("#4CAF50")
            percentage < 80 -> Color.parseColor("#FFC107")
            percentage < 100 -> Color.parseColor("#FF9800")
            else -> Color.parseColor("#F44336") // Red
        }
        holder.categoryBudgetProgress.setIndicatorColor(progressColor)

        holder.btnEditBudget.setOnClickListener {
            onEditClick(budget)
        }

        holder.btnDeleteBudget.setOnClickListener {
            onDeleteClick(budget)
        }
    }

    override fun getItemCount() = budgets.size

    fun updateData(newBudgets: ArrayList<Budget>) {
        this.budgets = newBudgets
        notifyDataSetChanged()
    }
}