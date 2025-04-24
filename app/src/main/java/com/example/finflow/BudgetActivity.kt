package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class BudgetsActivity : AppCompatActivity() {

    private lateinit var tvTotalBudget: TextView
    private lateinit var tvSpentAmount: TextView
    private lateinit var tvRemainingAmount: TextView
    private lateinit var tvBudgetPercentage: TextView
    private lateinit var tvBudgetMessage: TextView
    private lateinit var budgetProgressBar: LinearProgressIndicator
    private lateinit var budgetRecyclerView: RecyclerView
    private lateinit var emptyBudgetState: LinearLayout
    private lateinit var btnAddBudget: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView

    private var budgetsList = ArrayList<Budget>()
    private lateinit var adapter: BudgetsAdapter
    private var totalBudget = 0.0
    private var totalSpent = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        initViews()
        setupNavigation()
        setupListeners()

        loadBudgets()
        updateBudgetsList()
    }

    override fun onResume() {
        super.onResume()
        loadBudgets()
        updateBudgetsList()
    }

    private fun initViews() {
        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvSpentAmount = findViewById(R.id.tvSpentAmount)
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount)
        tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage)
        tvBudgetMessage = findViewById(R.id.tvBudgetMessage)
        budgetProgressBar = findViewById(R.id.budgetProgressBar)
        budgetRecyclerView = findViewById(R.id.budgetRecyclerView)
        emptyBudgetState = findViewById(R.id.emptyBudgetState)
        btnAddBudget = findViewById(R.id.btnAddBudget)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Setup RecyclerView
        adapter = BudgetsAdapter(
            budgetsList,
            onEditClick = { budget ->
                val intent = Intent(this, AddBudgetActivity::class.java)
                intent.putExtra("IS_EDIT_MODE", true)
                intent.putExtra("BUDGET_ID", budget.id)
                startActivity(intent)
            },
            onDeleteClick = { budget ->
                deleteBudget(budget)
            }
        )
        budgetRecyclerView.layoutManager = LinearLayoutManager(this)
        budgetRecyclerView.adapter = adapter
    }

    private fun setupNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_transactions -> {
                    startActivity(Intent(this, TransactionsActivity::class.java))
                    true
                }
                R.id.nav_budget -> true
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_budget
    }

    private fun setupListeners() {
        btnAddBudget.setOnClickListener {
            val intent = Intent(this, AddBudgetActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", false)
            startActivity(intent)
        }
    }

    private fun loadBudgets() {
        val sharedPrefs = getSharedPreferences("finflow_budgets", MODE_PRIVATE)
        val budgetsJson = sharedPrefs.getString("budgets_data", null)

        if (!budgetsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Budget>>() {}.type
            budgetsList = Gson().fromJson(budgetsJson, listType)
        } else {
            budgetsList.clear()
        }

        // Calculate spent amounts for each budget
        calculateSpentAmounts()
    }

    private fun calculateSpentAmounts() {
        // Load transactions to calculate spent amounts
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString("transactions_data", null)
        val transactions = ArrayList<Transaction>()

        if (!transactionsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
            transactions.addAll(Gson().fromJson(transactionsJson, listType))
        }

        // Get current month
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        // Calculate total budget and spent
        totalBudget = 0.0
        totalSpent = 0.0

        for (budget in budgetsList) {
            var spent = 0.0

            // Find transactions in the current month that match the budget category
            for (transaction in transactions) {
                val transCal = Calendar.getInstance()
                transCal.time = transaction.date

                if (transCal.get(Calendar.MONTH) == currentMonth &&
                    transCal.get(Calendar.YEAR) == currentYear &&
                    transaction.type == TransactionType.EXPENSE &&
                    transaction.category == budget.category) {
                    spent += transaction.amount
                }
            }

            budget.spent = spent
            totalBudget += budget.amount
            totalSpent += spent
        }

        updateSummaryDisplay()
    }

    private fun updateSummaryDisplay() {
        val remaining = totalBudget - totalSpent
        val percentage = if (totalBudget > 0) (totalSpent / totalBudget * 100).toInt() else 0

        tvTotalBudget.text = String.format("LKR %.2f", totalBudget)
        tvSpentAmount.text = String.format("LKR %.2f", totalSpent)
        tvRemainingAmount.text = String.format("LKR %.2f", remaining)
        tvBudgetPercentage.text = "$percentage%"
        budgetProgressBar.progress = percentage

        // Update message based on spent percentage
        when {
            percentage < 50 -> {
                tvBudgetMessage.text = "You're on track with your monthly budget!"
                budgetProgressBar.setIndicatorColor(getColor(R.color.primary))
            }
            percentage < 80 -> {
                tvBudgetMessage.text = "You're spending at a moderate pace."
                budgetProgressBar.setIndicatorColor(getColor(R.color.primary))
            }
            percentage < 100 -> {
                tvBudgetMessage.text = "You're approaching your budget limit!"
                budgetProgressBar.setIndicatorColor(getColor(R.color.warning_orange))
            }
            else -> {
                tvBudgetMessage.text = "You've exceeded your budget limit!"
                budgetProgressBar.setIndicatorColor(getColor(R.color.expense_red))
            }
        }
    }

    private fun updateBudgetsList() {
        adapter.updateData(budgetsList)

        if (budgetsList.isEmpty()) {
            emptyBudgetState.visibility = View.VISIBLE
            budgetRecyclerView.visibility = View.GONE
        } else {
            emptyBudgetState.visibility = View.GONE
            budgetRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun deleteBudget(budget: Budget) {
        budgetsList.remove(budget)

        // Save updated budgets
        val sharedPrefs = getSharedPreferences("finflow_budgets", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString("budgets_data", Gson().toJson(budgetsList))
        editor.apply()

        // Recalculate and update UI
        calculateSpentAmounts()
        updateBudgetsList()
    }
}