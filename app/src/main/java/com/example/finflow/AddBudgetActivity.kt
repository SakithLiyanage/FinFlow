package com.example.finflow

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class AddBudgetActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
    private lateinit var btnBack: ImageView
    private lateinit var etBudgetAmount: TextInputEditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var btnSaveBudget: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnDeleteBudget: MaterialButton

    private var isEditMode = false
    private var editBudgetId: String? = null
    private var selectedCategory: String? = null

    private val categoryList = listOf(
        CategoryItem("Food", "#FF5722", R.drawable.ic_food),
        CategoryItem("Transport", "#FF9800", R.drawable.ic_transport),
        CategoryItem("Shopping", "#F44336", R.drawable.ic_shopping),
        CategoryItem("Bills", "#E91E63", R.drawable.ic_bills),
        CategoryItem("Health", "#9C27B0", R.drawable.ic_health),
        CategoryItem("Education", "#673AB7", R.drawable.ic_education),
        CategoryItem("Entertainment", "#3F51B5", R.drawable.ic_entertainment),
        CategoryItem("Other", "#607D8B", R.drawable.ic_other)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_budget)

        initViews()
        setupCategoryChips()
        handleIntent()
        setupListeners()
    }

    private fun initViews() {
        toolbarTitle = findViewById(R.id.toolbarTitle)
        btnBack = findViewById(R.id.btnBack)
        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnCancel = findViewById(R.id.btnCancel)
        btnDeleteBudget = findViewById(R.id.btnDeleteBudget)
    }

    private fun setupCategoryChips() {
        categoryChipGroup.removeAllViews()

        for (category in categoryList) {
            val chip = Chip(this).apply {
                text = category.name
                isCheckable = true
                chipIcon = getDrawable(category.iconResId)
                chipIconTint = ColorStateList.valueOf(Color.parseColor(category.color))
                chipBackgroundColor = ColorStateList.valueOf(Color.WHITE)
                setTextColor(Color.parseColor("#212121")) // Dark text for all states

                // Set up selection listener
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category.name
                    } else if (selectedCategory == category.name) {
                        selectedCategory = null
                    }
                }
            }

            categoryChipGroup.addView(chip)
        }
    }

    private fun handleIntent() {
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)

        if (isEditMode) {
            toolbarTitle.text = "Edit Budget"
            btnDeleteBudget.visibility = View.VISIBLE

            editBudgetId = intent.getStringExtra("BUDGET_ID")
            editBudgetId?.let { budgetId ->
                loadBudgetDetails(budgetId)
            } ?: run {
                Toast.makeText(this, "Error loading budget", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            toolbarTitle.text = "Add Budget"
            btnDeleteBudget.visibility = View.GONE
        }
    }

    private fun loadBudgetDetails(budgetId: String) {
        val sharedPrefs = getSharedPreferences("finflow_budgets", MODE_PRIVATE)
        val budgetsJson = sharedPrefs.getString("budgets_data", null)

        if (!budgetsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Budget>>() {}.type
            val budgetsList: ArrayList<Budget> = Gson().fromJson(budgetsJson, listType)

            val budget = budgetsList.find { it.id == budgetId }

            budget?.let {
                // Fill form with budget details
                etBudgetAmount.setText(it.amount.toString())
                selectedCategory = it.category

                // Find and check the corresponding chip
                for (i in 0 until categoryChipGroup.childCount) {
                    val chip = categoryChipGroup.getChildAt(i) as Chip
                    if (chip.text == it.category) {
                        chip.isChecked = true
                        break
                    }
                }
            } ?: run {
                Toast.makeText(this, "Budget not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        btnDeleteBudget.setOnClickListener {
            deleteBudget()
        }
    }

    private fun saveBudget() {
        val amountText = etBudgetAmount.text.toString()

        if (TextUtils.isEmpty(amountText)) {
            Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val amount = amountText.toDouble()
            if (amount <= 0) {
                Toast.makeText(this, "Budget amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }

            val sharedPrefs = getSharedPreferences("finflow_budgets", MODE_PRIVATE)
            val budgetsJson = sharedPrefs.getString("budgets_data", null)
            val budgetsList = if (!budgetsJson.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<Budget>>() {}.type
                Gson().fromJson<ArrayList<Budget>>(budgetsJson, listType)
            } else {
                ArrayList()
            }

            if (isEditMode && editBudgetId != null) {
                // Update existing budget
                val index = budgetsList.indexOfFirst { it.id == editBudgetId }
                if (index != -1) {
                    val existingBudget = budgetsList[index]
                    budgetsList[index] = Budget(
                        id = existingBudget.id,
                        category = selectedCategory!!,
                        amount = amount,
                        spent = existingBudget.spent
                    )
                }
            } else {
                // Check if budget for this category already exists
                val existingBudget = budgetsList.find { it.category == selectedCategory }
                if (existingBudget != null) {
                    Toast.makeText(this,
                        "Budget for this category already exists. Please edit the existing budget instead.",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                // Add new budget
                val newBudget = Budget(
                    id = UUID.randomUUID().toString(),
                    category = selectedCategory!!,
                    amount = amount,
                    spent = 0.0
                )
                budgetsList.add(newBudget)
            }

            // Save updated list
            val editor = sharedPrefs.edit()
            editor.putString("budgets_data", Gson().toJson(budgetsList))
            editor.apply()

            Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Invalid amount format", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteBudget() {
        if (!isEditMode || editBudgetId == null) return

        val sharedPrefs = getSharedPreferences("finflow_budgets", MODE_PRIVATE)
        val budgetsJson = sharedPrefs.getString("budgets_data", null)

        if (!budgetsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Budget>>() {}.type
            val budgetsList: ArrayList<Budget> = Gson().fromJson(budgetsJson, listType)

            // Remove budget with matching ID
            val wasRemoved = budgetsList.removeIf { it.id == editBudgetId }

            if (wasRemoved) {
                // Save updated list
                val editor = sharedPrefs.edit()
                editor.putString("budgets_data", Gson().toJson(budgetsList))
                editor.apply()

                Toast.makeText(this, "Budget deleted", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error: Budget not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class CategoryItem(
        val name: String,
        val color: String,
        val iconResId: Int
    )
}