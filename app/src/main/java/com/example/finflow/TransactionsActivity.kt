package com.example.finflow

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.util.*

class TransactionsActivity : AppCompatActivity() {

    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var emptyStateText: TextView
    private lateinit var filterAllBtn: TextView
    private lateinit var filterIncomeBtn: TextView
    private lateinit var filterExpenseBtn: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnAddTransaction: ImageView
    private lateinit var fabAddTransaction: FloatingActionButton
    private lateinit var bottomNavigationView: BottomNavigationView

    private var transactionsList = ArrayList<Transaction>()
    private lateinit var adapter: CategoryTransactionAdapter

    // Currency formatter for LKR
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)

    // Filter state
    private var currentFilter = FilterType.ALL

    // Activity result handler for adding/editing transactions
    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh transactions after add/edit
            loadTransactions()
            updateFilterView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        // Configure currency formatter for LKR
        configureCurrencyFormatter()

        // Initialize views
        initViews()

        // Set up transactions list
        setupTransactionsList()

        // Load transactions
        loadTransactions()

        // Set up click listeners
        setupClickListeners()

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun configureCurrencyFormatter() {
        currencyFormatter.currency = Currency.getInstance("LKR")
        (currencyFormatter as java.text.DecimalFormat).positivePrefix = "LKR "
        currencyFormatter.maximumFractionDigits = 0
        currencyFormatter.minimumFractionDigits = 0
    }

    private fun initViews() {
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)
        emptyStateText = findViewById(R.id.emptyStateText)
        filterAllBtn = findViewById(R.id.filterAllBtn)
        filterIncomeBtn = findViewById(R.id.filterIncomeBtn)
        filterExpenseBtn = findViewById(R.id.filterExpenseBtn)
        btnBack = findViewById(R.id.btnBack)
        btnAddTransaction = findViewById(R.id.btnAddTransaction)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
    }

    private fun setupTransactionsList() {
        // Set up adapter for categorized transactions
        adapter = CategoryTransactionAdapter(
            currencyFormat = currencyFormatter,
            onItemClick = { transaction ->
                // Open edit transaction screen
                val intent = Intent(this, AddTransactionActivity::class.java)
                intent.putExtra("TRANSACTION_ID", transaction.id)
                intent.putExtra("IS_EDIT_MODE", true)
                addTransactionLauncher.launch(intent)
            },
            onDeleteClick = { transaction ->
                deleteTransaction(transaction)
            }
        )

        // Set up RecyclerView
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionsRecyclerView.adapter = adapter
    }

    private fun loadTransactions() {
        // Get transactions from SharedPreferences
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString("transactions_data", null)

        transactionsList.clear()

        if (!transactionsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
            val loadedList: ArrayList<Transaction> = Gson().fromJson(transactionsJson, listType)
            transactionsList.addAll(loadedList)
        }

        // Apply current filter
        updateFilteredList()
    }

    private fun updateFilteredList() {
        // Create filtered list based on current filter
        val filteredList = when (currentFilter) {
            FilterType.ALL -> transactionsList
            FilterType.INCOME -> transactionsList.filter { it.type == TransactionType.INCOME }
            FilterType.EXPENSE -> transactionsList.filter { it.type == TransactionType.EXPENSE }
        }

        // Sort by date (most recent first)
        val sortedList = filteredList.sortedByDescending { it.date }

        // Group by category
        val groupedTransactions = groupTransactionsByCategory(sortedList)

        // Update adapter
        adapter.updateTransactions(groupedTransactions)

        // Show/hide empty state
        if (sortedList.isEmpty()) {
            emptyStateView.visibility = View.VISIBLE
            when (currentFilter) {
                FilterType.ALL -> emptyStateText.text = "No transactions yet"
                FilterType.INCOME -> emptyStateText.text = "No income transactions"
                FilterType.EXPENSE -> emptyStateText.text = "No expense transactions"
            }
        } else {
            emptyStateView.visibility = View.GONE
        }
    }

    private fun groupTransactionsByCategory(transactions: List<Transaction>): List<TransactionListItem> {
        val result = mutableListOf<TransactionListItem>()

        // Group by category
        val groupedByCategory = transactions.groupBy { it.category }

        // For each category, add a header followed by transactions
        groupedByCategory.forEach { (category, transactionsInCategory) ->
            // Add category header
            result.add(TransactionListItem.CategoryHeader(
                category = category,
                totalAmount = transactionsInCategory.sumOf {
                    if (it.type == TransactionType.INCOME) it.amount else -it.amount
                }
            ))

            // Add transactions for this category
            transactionsInCategory.forEach { transaction ->
                result.add(TransactionListItem.TransactionItem(transaction))
            }
        }

        return result
    }

    private fun updateFilterView() {
        // Update filter button styles
        filterAllBtn.isSelected = currentFilter == FilterType.ALL
        filterIncomeBtn.isSelected = currentFilter == FilterType.INCOME
        filterExpenseBtn.isSelected = currentFilter == FilterType.EXPENSE

        // Update filtered list
        updateFilteredList()
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Add transaction button in toolbar
        btnAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        // Add transaction FAB
        fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        // Filter buttons
        filterAllBtn.setOnClickListener {
            currentFilter = FilterType.ALL
            updateFilterView()
        }

        filterIncomeBtn.setOnClickListener {
            currentFilter = FilterType.INCOME
            updateFilterView()
        }

        filterExpenseBtn.setOnClickListener {
            currentFilter = FilterType.EXPENSE
            updateFilterView()
        }
    }

    private fun setupBottomNavigation() {
        // Set Transactions as selected
        bottomNavigationView.selectedItemId = R.id.nav_transactions

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_transactions -> {
                    // Already on transactions
                    true
                }
                R.id.nav_budget -> {
                    val intent = Intent(this, BudgetActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_reports -> {
                    val intent = Intent(this, ReportsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        // Remove transaction from list
        transactionsList.removeIf { it.id == transaction.id }

        // Save updated list to SharedPreferences
        saveTransactionsToPrefs()

        // Update the view
        updateFilteredList()
    }

    private fun saveTransactionsToPrefs() {
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val transactionsJson = Gson().toJson(transactionsList)
        editor.putString("transactions_data", transactionsJson)
        editor.apply()
    }

    enum class FilterType {
        ALL,
        INCOME,
        EXPENSE
    }
}