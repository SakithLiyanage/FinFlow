package com.example.finflow

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var amountInputLayout: TextInputLayout
    private lateinit var etAmount: TextInputEditText
    private lateinit var dateInputLayout: TextInputLayout
    private lateinit var etDate: TextInputEditText
    private lateinit var notesInputLayout: TextInputLayout
    private lateinit var etNotes: TextInputEditText
    private lateinit var typeChipGroup: ChipGroup
    private lateinit var incomeChip: Chip
    private lateinit var expenseChip: Chip
    private lateinit var categoryLabel: TextView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var toolbar: Toolbar

    private var isEditMode = false
    private var currentTransactionId = ""
    private var selectedDate = Date()
    private var transactionType = TransactionType.EXPENSE
    private var selectedCategory = ""
    private var transactionsList = ArrayList<Transaction>()

    // Date formatter
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Income and expense categories
    private val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Other Income")
    private val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Education", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize views
        initViews()

        // Load transactions data
        loadTransactionsData()

        // Check if we're in edit mode
        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            currentTransactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""
            loadTransactionDetails()
        }

        // Set up UI for the appropriate mode
        setupUI()

        // Set up click listeners
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        titleInputLayout = findViewById(R.id.titleInputLayout)
        etTitle = findViewById(R.id.etTitle)
        amountInputLayout = findViewById(R.id.amountInputLayout)
        etAmount = findViewById(R.id.etAmount)
        dateInputLayout = findViewById(R.id.dateInputLayout)
        etDate = findViewById(R.id.etDate)
        notesInputLayout = findViewById(R.id.notesInputLayout)
        etNotes = findViewById(R.id.etNotes)
        typeChipGroup = findViewById(R.id.typeChipGroup)
        incomeChip = findViewById(R.id.incomeChip)
        expenseChip = findViewById(R.id.expenseChip)
        categoryLabel = findViewById(R.id.categoryLabel)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
        btnDelete = findViewById(R.id.btnDelete)
    }

    private fun setupUI() {
        // Set toolbar title
        toolbar.title = if (isEditMode) "Edit Transaction" else "Add Transaction"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Show delete button only in edit mode
        btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE

        // Set today's date by default for new transactions
        if (!isEditMode) {
            etDate.setText(dateFormat.format(selectedDate))
        }

        // Set default transaction type
        if (!isEditMode) {
            expenseChip.isChecked = true
            updateCategoryChips(expenseCategories)
        }
    }

    private fun loadTransactionsData() {
        // Get transactions from SharedPreferences
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val transactionsJson = sharedPrefs.getString("transactions_data", null)

        if (!transactionsJson.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
            transactionsList = Gson().fromJson(transactionsJson, listType)
        }
    }


    private fun loadTransactionDetails() {
        val transaction = transactionsList.find { it.id == currentTransactionId }
        if (transaction != null) {
            // Fill in details
            etTitle.setText(transaction.title)
            etAmount.setText(transaction.amount.toString())
            selectedDate = transaction.date
            etDate.setText(dateFormat.format(selectedDate))
            etNotes.setText(transaction.notes)
            transactionType = transaction.type
            selectedCategory = transaction.category

            // Set transaction type
            if (transaction.type == TransactionType.INCOME) {
                incomeChip.isChecked = true
                updateCategoryChips(incomeCategories)
            } else {
                expenseChip.isChecked = true
                updateCategoryChips(expenseCategories)
            }

            // Select the correct category chip
            for (i in 0 until categoryChipGroup.childCount) {
                val chip = categoryChipGroup.getChildAt(i) as Chip
                if (chip.text == selectedCategory) {
                    chip.isChecked = true
                    break
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Date selection
        etDate.setOnClickListener {
            showDatePicker()
        }

        // Transaction type selection
        typeChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.incomeChip -> {
                        transactionType = TransactionType.INCOME
                        updateCategoryChips(incomeCategories)
                    }
                    R.id.expenseChip -> {
                        transactionType = TransactionType.EXPENSE
                        updateCategoryChips(expenseCategories)
                    }
                }
            }
        }

        // Category selection
        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds[0])
                selectedCategory = chip.text.toString()
            } else {
                selectedCategory = ""
            }
        }

        // Save button
        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }

        // Cancel button
        btnCancel.setOnClickListener {
            finish()
        }

        // Delete button
        btnDelete.setOnClickListener {
            deleteTransaction()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                selectedDate = calendar.time
                etDate.setText(dateFormat.format(selectedDate))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun updateCategoryChips(categories: List<String>) {
        // Clear existing chips
        categoryChipGroup.removeAllViews()

        // Add new category chips
        for (category in categories) {
            val chip = Chip(this)
            chip.text = category
            chip.isCheckable = true
            chip.isCheckedIconVisible = true

            // If we're in edit mode and this is the selected category, check it
            if (category == selectedCategory) {
                chip.isChecked = true
            }

            categoryChipGroup.addView(chip)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate title
        if (etTitle.text.isNullOrBlank()) {
            titleInputLayout.error = "Title is required"
            isValid = false
        } else {
            titleInputLayout.error = null
        }

        // Validate amount
        if (etAmount.text.isNullOrBlank()) {
            amountInputLayout.error = "Amount is required"
            isValid = false
        } else {
            try {
                val amount = etAmount.text.toString().toDouble()
                if (amount <= 0) {
                    amountInputLayout.error = "Amount must be greater than 0"
                    isValid = false
                } else {
                    amountInputLayout.error = null
                }
            } catch (e: NumberFormatException) {
                amountInputLayout.error = "Invalid amount"
                isValid = false
            }
        }

        // Validate category
        if (selectedCategory.isBlank()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun saveTransaction() {
        val title = etTitle.text.toString()
        val amount = etAmount.text.toString().toDouble()
        val notes = etNotes.text.toString()

        if (isEditMode) {
            // Update existing transaction
            val index = transactionsList.indexOfFirst { it.id == currentTransactionId }
            if (index != -1) {
                transactionsList[index] = Transaction(
                    id = currentTransactionId,
                    title = title,
                    amount = amount,
                    type = transactionType,
                    category = selectedCategory,
                    date = selectedDate,
                    notes = notes
                )
            }
        } else {
            // Create new transaction
            val newTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                title = title,
                amount = amount,
                type = transactionType,
                category = selectedCategory,
                date = selectedDate,
                notes = notes
            )
            transactionsList.add(newTransaction)
        }

        // Save updated list to SharedPreferences
        saveTransactionsToPrefs()

        // Set result and finish
        setResult(RESULT_OK)
        finish()
    }

    private fun deleteTransaction() {
        if (isEditMode) {
            // Remove transaction from list
            transactionsList.removeIf { it.id == currentTransactionId }

            // Save updated list to SharedPreferences
            saveTransactionsToPrefs()

            // Set result and finish
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun saveTransactionsToPrefs() {
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val transactionsJson = Gson().toJson(transactionsList)
        editor.putString("transactions_data", transactionsJson)
        editor.apply()
    }
}