package com.example.finflow

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class CategoryTransactionAdapter(
    private val currencyFormat: NumberFormat,
    private val onItemClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_VIEW_TYPE_HEADER = 0
    private val ITEM_VIEW_TYPE_TRANSACTION = 1
    private var items = listOf<TransactionListItem>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun updateTransactions(newItems: List<TransactionListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TransactionListItem.CategoryHeader -> ITEM_VIEW_TYPE_HEADER
            is TransactionListItem.TransactionItem -> ITEM_VIEW_TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_category_header, parent, false)
                CategoryHeaderViewHolder(view)
            }
            ITEM_VIEW_TYPE_TRANSACTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_trasaction_with_delete, parent, false)
                TransactionViewHolder(view)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryHeaderViewHolder -> {
                val headerItem = items[position] as TransactionListItem.CategoryHeader
                holder.bind(headerItem)
            }
            is TransactionViewHolder -> {
                val transactionItem = items[position] as TransactionListItem.TransactionItem
                holder.bind(transactionItem.transaction)
            }
        }
    }

    override fun getItemCount() = items.size

    inner class CategoryHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCategoryTotal: TextView = itemView.findViewById(R.id.tvCategoryTotal)
        private val categoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)

        fun bind(item: TransactionListItem.CategoryHeader) {
            tvCategoryName.text = item.category

            val formattedAmount = currencyFormat.format(Math.abs(item.totalAmount))

            if (item.totalAmount >= 0) {
                tvCategoryTotal.setTextColor(ContextCompat.getColor(itemView.context, R.color.income_green))
                tvCategoryTotal.text = "+ $formattedAmount"
            } else {
                tvCategoryTotal.setTextColor(ContextCompat.getColor(itemView.context, R.color.expense_red))
                tvCategoryTotal.text = "- $formattedAmount"
            }

            val iconResId = getCategoryIcon(item.category)
            categoryIcon.setImageResource(iconResId)

            val colorResId = getCategoryColor(item.category)
            categoryIcon.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, colorResId)
            )
        }

        private fun getCategoryIcon(category: String): Int {
            return when (category.lowercase()) {
                "food", "groceries", "dining" -> R.drawable.ic_food
                "bills", "utilities" -> R.drawable.ic_bills
                "transport", "travel" -> R.drawable.ic_transport
                "shopping" -> R.drawable.ic_shopping
                "entertainment" -> R.drawable.ic_entertainment
                "health", "medical" -> R.drawable.ic_health
                "salary", "income" -> R.drawable.ic_income
                "info" -> R.drawable.ic_info
                else -> R.drawable.ic_category
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category.lowercase()) {
                "food", "groceries", "dining" -> R.color.category_food
                "bills", "utilities" -> R.color.category_bills
                "transport", "travel" -> R.color.category_transport
                "shopping" -> R.color.category_shopping
                "entertainment" -> R.color.category_entertainment
                "health", "medical" -> R.color.category_health
                "salary", "income" -> R.color.income_green
                "info" -> R.color.text_secondary
                else -> R.color.primary
            }
        }
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTransactionTitle)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        private val btnDelete: ImageView = itemView.findViewById(R.id.btnDeleteTransaction)

        fun bind(transaction: Transaction) {
            tvTitle.text = transaction.title
            tvDate.text = dateFormat.format(transaction.date)

            val formattedAmount = currencyFormat.format(transaction.amount)

            if (transaction.type == TransactionType.INCOME) {
                tvAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.income_green))
                tvAmount.text = "+ $formattedAmount"
            } else {
                tvAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.expense_red))
                tvAmount.text = "- $formattedAmount"
            }

            itemView.setOnClickListener {
                onItemClick(transaction)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(transaction)
            }
        }
    }
}