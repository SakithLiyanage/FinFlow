package com.example.finflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.InputStreamReader
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportsActivity : AppCompatActivity() {

    private val TAG = "ReportsActivity"

    // UI Elements
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvNetBalance: TextView
    private lateinit var rvCategorySummary: RecyclerView
    private lateinit var btnExportJson: MaterialButton
    private lateinit var btnExportText: MaterialButton
    private lateinit var btnImportData: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView

    // Data
    private var transactionsList = ArrayList<Transaction>()
    private var categorySummaries = ArrayList<CategorySummary>()

    // Currency formatter for LKR
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    // Create document launcher for exports
    private val createJsonDocument = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { exportToJson(it) }
    }

    private val createTextDocument = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let { exportToText(it) }
    }

    // Open document launcher for imports
    private val openJsonDocument = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importFromJson(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        try {
            // Configure currency formatter
            configureCurrencyFormatter()

            // Initialize views
            initViews()

            // Load data
            loadTransactions()

            // Analyze data
            analyzeTransactions()

            // Setup category summary
            setupCategorySummaryList()

            // Update financial summary
            updateFinancialSummary()

            // Setup click listeners
            setupClickListeners()

            // Setup bottom navigation
            setupBottomNavigation()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ReportsActivity", e)
            Toast.makeText(this, "Error loading reports", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureCurrencyFormatter() {
        try {
            currencyFormatter.currency = java.util.Currency.getInstance("LKR")
            (currencyFormatter as java.text.DecimalFormat).positivePrefix = "LKR "
            currencyFormatter.maximumFractionDigits = 0
            currencyFormatter.minimumFractionDigits = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring currency formatter", e)
        }
    }

    private fun initViews() {
        // Financial summary views
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvNetBalance = findViewById(R.id.tvNetBalance)

        // Category summary recycler view
        rvCategorySummary = findViewById(R.id.rvCategorySummary)

        // Data management buttons
        btnExportJson = findViewById(R.id.btnExportJson)
        btnExportText = findViewById(R.id.btnExportText)
        btnImportData = findViewById(R.id.btnImportData)

        // Bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun loadTransactions() {
        try {
            // Load transactions from SharedPreferences
            val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
            val transactionsJson = sharedPrefs.getString("transactions_data", null)

            transactionsList.clear()

            if (!transactionsJson.isNullOrEmpty()) {
                val type = object : TypeToken<ArrayList<Transaction>>() {}.type
                val loadedTransactions: ArrayList<Transaction> = Gson().fromJson(transactionsJson, type)
                transactionsList.addAll(loadedTransactions)

                Log.d(TAG, "Loaded ${transactionsList.size} transactions")
            } else {
                Log.d(TAG, "No transactions found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading transactions", e)
            Toast.makeText(this, "Error loading transactions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeTransactions() {
        try {
            // Category totals for expenses
            val categoryExpenses = HashMap<String, Double>()
            val categoryIcons = HashMap<String, Int>()
            val categoryColors = HashMap<String, Int>()

            // Setup default category icons and colors
            setupCategoryDefaults(categoryIcons, categoryColors)

            // Calculate category totals
            for (transaction in transactionsList) {
                if (transaction.type == TransactionType.EXPENSE) {
                    val category = transaction.category
                    val currentTotal = categoryExpenses[category] ?: 0.0
                    categoryExpenses[category] = currentTotal + transaction.amount
                }
            }

            // Create category summaries
            categorySummaries.clear()
            for ((category, amount) in categoryExpenses) {
                categorySummaries.add(
                    CategorySummary(
                        name = category,
                        amount = amount,
                        iconResId = categoryIcons[category] ?: R.drawable.ic_category,
                        colorResId = categoryColors[category] ?: R.color.primary
                    )
                )
            }

            // Sort by amount (highest first)
            categorySummaries.sortByDescending { it.amount }

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing transactions", e)
        }
    }

    private fun setupCategoryDefaults(
        categoryIcons: HashMap<String, Int>,
        categoryColors: HashMap<String, Int>
    ) {
        // Add mappings for common categories
        categoryIcons["Food"] = R.drawable.ic_food
        categoryColors["Food"] = R.color.category_food

        categoryIcons["Bills"] = R.drawable.ic_bills
        categoryColors["Bills"] = R.color.category_bills

        categoryIcons["Transport"] = R.drawable.ic_transport
        categoryColors["Transport"] = R.color.category_transport

        categoryIcons["Shopping"] = R.drawable.ic_shopping
        categoryColors["Shopping"] = R.color.category_shopping

        categoryIcons["Entertainment"] = R.drawable.ic_entertainment
        categoryColors["Entertainment"] = R.color.category_entertainment

        categoryIcons["Health"] = R.drawable.ic_health
        categoryColors["Health"] = R.color.category_health

        categoryIcons["Salary"] = R.drawable.ic_income
        categoryColors["Salary"] = R.color.income_green

        categoryIcons["Freelance"] = R.drawable.ic_income
        categoryColors["Freelance"] = R.color.income_green
    }

    private fun setupCategorySummaryList() {
        try {
            val adapter = CategorySummaryAdapter(categorySummaries, currencyFormatter)
            rvCategorySummary.layoutManager = LinearLayoutManager(this)
            rvCategorySummary.adapter = adapter
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up category summary list", e)
        }
    }

    private fun updateFinancialSummary() {
        try {
            var totalIncome = 0.0
            var totalExpense = 0.0

            // Calculate totals
            for (transaction in transactionsList) {
                if (transaction.type == TransactionType.INCOME) {
                    totalIncome += transaction.amount
                } else {
                    totalExpense += transaction.amount
                }
            }

            // Update UI
            tvTotalIncome.text = currencyFormatter.format(totalIncome)
            tvTotalExpenses.text = currencyFormatter.format(totalExpense)

            val netBalance = totalIncome - totalExpense
            tvNetBalance.text = currencyFormatter.format(netBalance)

            // Set net balance color
            if (netBalance >= 0) {
                tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.income_green))
            } else {
                tvNetBalance.setTextColor(ContextCompat.getColor(this, R.color.expense_red))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating financial summary", e)
        }
    }

    private fun setupClickListeners() {
        try {
            // Back button
            findViewById<ImageView>(R.id.btnBack).setOnClickListener {
                finish()
            }

            // Export as JSON button
            btnExportJson.setOnClickListener {
                if (transactionsList.isEmpty()) {
                    Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val filename = "finflow_transactions_${formatter.format(Date())}.json"
                createJsonDocument.launch(filename)
            }

            // Export as Text button
            btnExportText.setOnClickListener {
                if (transactionsList.isEmpty()) {
                    Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val filename = "finflow_transactions_${formatter.format(Date())}.txt"
                createTextDocument.launch(filename)
            }

            // Import data button
            btnImportData.setOnClickListener {
                openJsonDocument.launch(arrayOf("application/json"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up click listeners", e)
        }
    }

    private fun setupBottomNavigation() {
        try {
            // Set Reports as selected
            bottomNavigationView.selectedItemId = R.id.nav_reports

            bottomNavigationView.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.nav_home -> {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.nav_transactions -> {
                        startActivity(Intent(this, TransactionsActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_budget -> {
                        startActivity(Intent(this, BudgetActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_reports -> {
                        // Already on reports
                        true
                    }
                    else -> false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bottom navigation", e)
        }
    }

    private fun exportToJson(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(transactionsList)
                outputStream.write(jsonString.toByteArray())

                Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data to JSON", e)
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun exportToText(uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val outputText = StringBuilder()

                outputText.append("FINFLOW TRANSACTION EXPORT\n")
                outputText.append("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                outputText.append("Total Transactions: ${transactionsList.size}\n\n")

                // Add summary
                var totalIncome = 0.0
                var totalExpense = 0.0

                for (transaction in transactionsList) {
                    if (transaction.type == TransactionType.INCOME) {
                        totalIncome += transaction.amount
                    } else {
                        totalExpense += transaction.amount
                    }
                }

                outputText.append("SUMMARY:\n")
                outputText.append("Total Income: ${currencyFormatter.format(totalIncome)}\n")
                outputText.append("Total Expenses: ${currencyFormatter.format(totalExpense)}\n")
                outputText.append("Net Balance: ${currencyFormatter.format(totalIncome - totalExpense)}\n\n")

                outputText.append("TRANSACTIONS:\n")
                outputText.append("---------------------------------------------------\n")

                // Format for date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Add each transaction
                for (transaction in transactionsList.sortedByDescending { it.date }) {
                    outputText.append("Date: ${dateFormat.format(transaction.date)}\n")
                    outputText.append("Type: ${transaction.type}\n")
                    outputText.append("Title: ${transaction.title}\n")
                    outputText.append("Category: ${transaction.category}\n")
                    outputText.append("Amount: ${currencyFormatter.format(transaction.amount)}\n")
                    if (!transaction.notes.isNullOrEmpty()) {
                        outputText.append("Notes: ${transaction.notes}\n")
                    }
                    outputText.append("---------------------------------------------------\n")
                }

                outputStream.write(outputText.toString().toByteArray())
                Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data to text", e)
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromJson(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            inputStream?.close()

            val jsonString = stringBuilder.toString()

            // Validate JSON format
            try {
                val type = object : TypeToken<ArrayList<Transaction>>() {}.type
                val importedTransactions: ArrayList<Transaction> = Gson().fromJson(jsonString, type)

                if (importedTransactions.isEmpty()) {
                    Toast.makeText(this, "No valid transactions found in file", Toast.LENGTH_SHORT).show()
                    return
                }

                // Ask for confirmation
                AlertDialog.Builder(this)
                    .setTitle("Import Data")
                    .setMessage("This will replace your current transaction data with ${importedTransactions.size} transactions from the backup. Do you want to continue?")
                    .setPositiveButton("Import") { _, _ ->
                        // Replace the current transactions
                        saveTransactions(importedTransactions)
                        Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show()

                        // Reload the data and refresh UI
                        loadTransactions()
                        analyzeTransactions()
                        setupCategorySummaryList()
                        updateFinancialSummary()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()

            } catch (e: Exception) {
                Log.e(TAG, "Invalid JSON format", e)
                Toast.makeText(this, "Invalid backup file format", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error importing data from JSON", e)
            Toast.makeText(this, "Failed to import data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTransactions(transactions: ArrayList<Transaction>) {
        try {
            val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            val transactionsJson = Gson().toJson(transactions)
            editor.putString("transactions_data", transactionsJson)
            editor.apply()

            Log.d(TAG, "Saved ${transactions.size} transactions")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transactions", e)
        }
    }
}