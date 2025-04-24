package com.example.finflow

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TransactionsActivity : AppCompatActivity() {

    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var btnMonthSelector: LinearLayout
    private lateinit var tvCurrentMonth: TextView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvNetBalance: TextView
    private lateinit var btnPrevMonth: ImageView
    private lateinit var btnNextMonth: ImageView
    private lateinit var chipGroup: ChipGroup
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var fabAddTransaction: FloatingActionButton

    private var transactionsList = ArrayList<Transaction>()
    private var filteredTransactions = ArrayList<Transaction>()
    private lateinit var adapter: TransactionsAdapter

    private var currentFilter = FilterType.ALL
    private val calendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val currencyFormatter = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("LKR")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        initViews()
        setupNavigation()
        setupListeners()

        // Check if we should pre-select a specific filter type
        handleIntent()
        loadTransactions()
        updateMonthDisplay()
        filterTransactions()
    }

    override fun onResume() {
        super.onResume()
        loadTransactions()
        filterTransactions()
    }

    private fun initViews() {
        transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        btnMonthSelector = findViewById(R.id.monthSelectorLayout)
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvNetBalance = findViewById(R.id.tvNetBalance)
        btnPrevMonth = findViewById(R.id.btnPrevMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        chipGroup = findViewById(R.id.chipGroupFilter)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        fabAddTransaction = findViewById(R.id.fabAddTransaction)

        // Setup RecyclerView
        adapter = TransactionsAdapter(filteredTransactions) { transaction ->
            // Handle transaction click
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", true)
            intent.putExtra("TRANSACTION_ID", transaction.id)
            startActivity(intent)
        }
        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionsRecyclerView.adapter = adapter
    }

    private fun setupNavigation() {
        bottomNavigationView.selectedItemId = R.id.nav_transactions
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    false
                }
                R.id.nav_transactions -> true
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetsActivity::class.java))
                    false
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        fabAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthDisplay()
            filterTransactions()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthDisplay()
            filterTransactions()
        }

        btnMonthSelector.setOnClickListener {
            showMonthYearPicker()
        }

        // Setup chip group listener
        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilter = when (checkedId) {
                R.id.chipIncome -> FilterType.INCOME
                R.id.chipExpense -> FilterType.EXPENSE
                else -> FilterType.ALL
            }
            filterTransactions()
        }

        // Default to "All" filter
        chipGroup.check(R.id.chipAll)
    }

    private fun handleIntent() {
        // Check if we should pre-select a filter type
        val filterType = intent.getStringExtra("FILTER_TYPE")
        when (filterType) {
            "INCOME" -> {
                chipGroup.check(R.id.chipIncome)
                currentFilter = FilterType.INCOME
            }
            "EXPENSE" -> {
                chipGroup.check(R.id.chipExpense)
                currentFilter = FilterType.EXPENSE
            }
            else -> {
                chipGroup.check(R.id.chipAll)
                currentFilter = FilterType.ALL
            }
        }
    }

    private fun loadTransactions() {
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString("transactions_data", null)

        if (!transactionsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
            transactionsList = Gson().fromJson(transactionsJson, listType)
        } else {
            transactionsList.clear()
        }
    }

    private fun updateMonthDisplay() {
        tvCurrentMonth.text = monthYearFormat.format(calendar.time)
    }

    private fun filterTransactions() {
        // Filter by month
        val startMonth = calendar.clone() as Calendar
        startMonth.set(Calendar.DAY_OF_MONTH, 1)
        startMonth.set(Calendar.HOUR_OF_DAY, 0)
        startMonth.set(Calendar.MINUTE, 0)
        startMonth.set(Calendar.SECOND, 0)
        startMonth.set(Calendar.MILLISECOND, 0)

        val endMonth = calendar.clone() as Calendar
        endMonth.set(Calendar.DAY_OF_MONTH, endMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endMonth.set(Calendar.HOUR_OF_DAY, 23)
        endMonth.set(Calendar.MINUTE, 59)
        endMonth.set(Calendar.SECOND, 59)
        endMonth.set(Calendar.MILLISECOND, 999)

        // Filter by month and type
        filteredTransactions = when (currentFilter) {
            FilterType.INCOME -> transactionsList.filter {
                it.date.time >= startMonth.timeInMillis &&
                        it.date.time <= endMonth.timeInMillis &&
                        it.type == TransactionType.INCOME
            } as ArrayList<Transaction>
            FilterType.EXPENSE -> transactionsList.filter {
                it.date.time >= startMonth.timeInMillis &&
                        it.date.time <= endMonth.timeInMillis &&
                        it.type == TransactionType.EXPENSE
            } as ArrayList<Transaction>
            else -> transactionsList.filter {
                it.date.time >= startMonth.timeInMillis &&
                        it.date.time <= endMonth.timeInMillis
            } as ArrayList<Transaction>
        }

        // Sort by date (newest first)
        filteredTransactions.sortByDescending { it.date }

        // Update UI
        updateTransactionsList()
        calculateSummary()
    }

    private fun updateTransactionsList() {
        adapter.updateData(filteredTransactions)

        if (filteredTransactions.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            transactionsRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            transactionsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun calculateSummary() {
        var totalIncome = 0.0
        var totalExpense = 0.0

        // Calculate for the filtered month
        val startMonth = calendar.clone() as Calendar
        startMonth.set(Calendar.DAY_OF_MONTH, 1)
        startMonth.set(Calendar.HOUR_OF_DAY, 0)
        startMonth.set(Calendar.MINUTE, 0)
        startMonth.set(Calendar.SECOND, 0)
        startMonth.set(Calendar.MILLISECOND, 0)

        val endMonth = calendar.clone() as Calendar
        endMonth.set(Calendar.DAY_OF_MONTH, endMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        endMonth.set(Calendar.HOUR_OF_DAY, 23)
        endMonth.set(Calendar.MINUTE, 59)
        endMonth.set(Calendar.SECOND, 59)
        endMonth.set(Calendar.MILLISECOND, 999)

        for (transaction in transactionsList) {
            if (transaction.date.time >= startMonth.timeInMillis &&
                transaction.date.time <= endMonth.timeInMillis) {
                if (transaction.type == TransactionType.INCOME) {
                    totalIncome += transaction.amount
                } else {
                    totalExpense += transaction.amount
                }
            }
        }

        val netBalance = totalIncome - totalExpense

        tvTotalIncome.text = currencyFormatter.format(totalIncome)
        tvTotalExpenses.text = currencyFormatter.format(totalExpense)
        tvNetBalance.text = currencyFormatter.format(netBalance)
    }

    private fun showMonthYearPicker() {
        val monthYearPicker = DatePickerDialog(
            this,
            { _, year, month, _ ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                updateMonthDisplay()
                filterTransactions()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        )

        // Try to hide the day picker
        try {
            val datePickerField = DatePickerDialog::class.java.getDeclaredField("mDatePicker")
            datePickerField.isAccessible = true
            val datePicker = datePickerField.get(monthYearPicker) as android.widget.DatePicker
            val daySpinner = datePicker.findViewById<View>(
                Resources.getSystem().getIdentifier("day", "id", "android")
            )
            if (daySpinner != null) {
                daySpinner.visibility = View.GONE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        monthYearPicker.show()
    }

    enum class FilterType {
        ALL, INCOME, EXPENSE
    }
}