package com.example.finflow

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransactionsAdapter(
    private var transactions: ArrayList<Transaction>,
    private val onTransactionClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val categoryColorMap = mapOf(
        "Salary" to "#4CAF50",
        "Freelance" to "#8BC34A",
        "Investments" to "#009688",
        "Gifts" to "#00BCD4",
        "Other Income" to "#03A9F4",

        "Food" to "#FF5722",
        "Transport" to "#FF9800",
        "Shopping" to "#F44336",
        "Bills" to "#E91E63",
        "Health" to "#9C27B0",
        "Education" to "#673AB7",
        "Entertainment" to "#3F51B5",
        "Other" to "#607D8B"
    )

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryIndicator: View = itemView.findViewById(R.id.categoryIndicator)
        val tvTransactionTitle: TextView = itemView.findViewById(R.id.tvTransactionTitle)
        val tvTransactionCategory: TextView = itemView.findViewById(R.id.tvTransactionCategory)
        val tvTransactionDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        val tvTransactionAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        val btnDeleteTransaction: MaterialButton = itemView.findViewById(R.id.btnDeleteTransaction)
        val transactionContainer: View = itemView.findViewById(R.id.transactionContainer)

        val tvTransactionNote: TextView? = itemView.findViewById(R.id.tvTransactionNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvTransactionTitle.text = transaction.title
        holder.tvTransactionCategory.text = transaction.category
        holder.tvTransactionDate.text = formatDate(transaction.date)

        holder.tvTransactionNote?.let {
            if (transaction.notes.isNotEmpty()) {
                it.text = transaction.notes
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
        }

        if (transaction.type == TransactionType.INCOME) {
            holder.tvTransactionAmount.text = String.format("+LKR %.2f", transaction.amount)
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#4CAF50")) // Green for income
        } else {
            holder.tvTransactionAmount.text = String.format("-LKR %.2f", transaction.amount)
            holder.tvTransactionAmount.setTextColor(Color.parseColor("#F44336")) // Red for expense
        }

        val categoryColor = categoryColorMap[transaction.category] ?: "#607D8B" // Default gray
        holder.categoryIndicator.backgroundTintList = ColorStateList.valueOf(Color.parseColor(categoryColor))

        holder.transactionContainer.setOnClickListener {
            onTransactionClick(transaction)
        }

        holder.btnDeleteTransaction.setOnClickListener {
            showDeleteConfirmationDialog(holder.itemView.context as AppCompatActivity, position, transaction)
        }
    }

    private fun formatDate(date: Date): String {
        val today = Calendar.getInstance()
        val transactionDate = Calendar.getInstance()
        transactionDate.time = date

        return when {
            isSameDay(today, transactionDate) -> "Today"
            isYesterday(today, transactionDate) -> "Yesterday"
            else -> dateFormat.format(date)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, otherDate: Calendar): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        return isSameDay(yesterday, otherDate)
    }

    private fun showDeleteConfirmationDialog(activity: AppCompatActivity, position: Int, transaction: Transaction) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(activity)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction? This action cannot be undone.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteTransaction(activity, position, transaction)
            }
            .show()
    }

    private fun deleteTransaction(activity: AppCompatActivity, position: Int, transaction: Transaction) {
        val sharedPrefs = activity.getSharedPreferences(
            "finflow_transactions",
            AppCompatActivity.MODE_PRIVATE
        )

        val transactionsJson = sharedPrefs.getString("transactions_data", null)
        if (!transactionsJson.isNullOrEmpty()) {
            val gson = Gson()
            val typeToken = object : TypeToken<ArrayList<Transaction>>() {}
            val allTransactions: ArrayList<Transaction> = gson.fromJson(transactionsJson, typeToken.type)

            allTransactions.removeIf { it.id == transaction.id }

            val editor = sharedPrefs.edit()
            editor.putString("transactions_data", gson.toJson(allTransactions))
            editor.apply()

            transactions.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, transactions.size)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: ArrayList<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}