package com.example.finflow

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
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
    private lateinit var incomeChip: Chip
    private lateinit var expenseChip: Chip
    private lateinit var categoryLabel: TextView
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var toolbar: Toolbar

    private val currentDateTime = "2025-04-24 06:31:36"
    private val currentUser = "SakithLiyanage"
    private val TAG = "AddTransactionActivity"

    private var isEditMode = false
    private var currentTransactionId = ""
    private var selectedDate = Date()
    private var transactionType = TransactionType.EXPENSE
    private var selectedCategory = ""
    private var transactionsList = ArrayList<Transaction>()

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private val incomeCategories = listOf("Salary", "Freelance", "Investments", "Gifts", "Other Income")
    private val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Health", "Education", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        Log.d(TAG, "Activity created at $currentDateTime by $currentUser")

        initViews()

        loadTransactionsData()

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        if (isEditMode) {
            currentTransactionId = intent.getStringExtra("TRANSACTION_ID") ?: ""
            loadTransactionDetails()
        }

        setupUI()

        setupClickListeners()
    }

    private fun initViews() {
        try {
            toolbar = findViewById(R.id.toolbar)
            titleInputLayout = findViewById(R.id.titleInputLayout)
            etTitle = findViewById(R.id.etTitle)
            amountInputLayout = findViewById(R.id.amountInputLayout)
            etAmount = findViewById(R.id.etAmount)
            dateInputLayout = findViewById(R.id.dateInputLayout)
            etDate = findViewById(R.id.etDate)
            notesInputLayout = findViewById(R.id.notesInputLayout)
            etNotes = findViewById(R.id.etNotes)

            incomeChip = findViewById(R.id.incomeChip)
            expenseChip = findViewById(R.id.expenseChip)

            categoryLabel = findViewById(R.id.categoryLabel)
            categoryChipGroup = findViewById(R.id.categoryChipGroup)
            btnCancel = findViewById(R.id.btnCancel)
            btnSave = findViewById(R.id.btnSave)
            btnDelete = findViewById(R.id.btnDelete)

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
            Toast.makeText(this, "Error initializing form", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUI() {
        toolbar.title = if (isEditMode) "Edit Transaction" else "Add Transaction"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        btnDelete.visibility = if (isEditMode) View.VISIBLE else View.GONE

        if (!isEditMode) {
            etDate.setText(dateFormat.format(selectedDate))
        }

        if (!isEditMode) {
            expenseChip.isChecked = true
            updateCategoryChips(expenseCategories)
        }
    }

    private fun loadTransactionsData() {
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
            etTitle.setText(transaction.title)
            etAmount.setText(transaction.amount.toString())
            selectedDate = transaction.date
            etDate.setText(dateFormat.format(selectedDate))
            etNotes.setText(transaction.notes)
            transactionType = transaction.type
            selectedCategory = transaction.category

            if (transaction.type == TransactionType.INCOME) {
                incomeChip.isChecked = true
                expenseChip.isChecked = false
                updateCategoryChips(incomeCategories)
            } else {
                expenseChip.isChecked = true
                incomeChip.isChecked = false
                updateCategoryChips(expenseCategories)
            }

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
        etDate.setOnClickListener {
            showDatePicker()
        }

        incomeChip.setOnClickListener {
            incomeChip.isChecked = true
            expenseChip.isChecked = false

            transactionType = TransactionType.INCOME
            updateCategoryChips(incomeCategories)

            Log.d(TAG, "Transaction type set to INCOME")
        }

        expenseChip.setOnClickListener {
            expenseChip.isChecked = true
            incomeChip.isChecked = false

            transactionType = TransactionType.EXPENSE
            updateCategoryChips(expenseCategories)

            Log.d(TAG, "Transaction type set to EXPENSE")
        }

        categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds[0])
                selectedCategory = chip.text.toString()
                Log.d(TAG, "Selected category: $selectedCategory")
            } else {
                selectedCategory = ""
            }
        }

        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTransaction()
            }
        }

        btnCancel.setOnClickListener {
            finish()
        }

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
        try {
            categoryChipGroup.removeAllViews()

            for (category in categories) {
                val chip = Chip(this)
                chip.text = category
                chip.isCheckable = true
                chip.isCheckedIconVisible = true

                if (category == selectedCategory) {
                    chip.isChecked = true
                }

                categoryChipGroup.addView(chip)
            }

            val labelText = if (transactionType == TransactionType.INCOME) "Income Category" else "Expense Category"
            categoryLabel.text = labelText

            Log.d(TAG, "Updated categories for type: $transactionType")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category chips", e)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (etTitle.text.isNullOrBlank()) {
            titleInputLayout.error = "Title is required"
            isValid = false
        } else {
            titleInputLayout.error = null
        }

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
            val index = transactionsList.indexOfFirst { it.id == currentTransactionId }
            if (index != -1) {
                transactionsList[index] = Transaction(
                    id = currentTransactionId,
                    title = title,
                    amount = amount,
                    type = transactionType,
                    category = selectedCategory,
                    date = selectedDate,
                    notes = notes,
                    userId = currentUser
                )

                Log.d(TAG, "Updated transaction ID: $currentTransactionId")
            }
        } else {
            val newTransaction = Transaction(
                id = UUID.randomUUID().toString(),
                title = title,
                amount = amount,
                type = transactionType,
                category = selectedCategory,
                date = selectedDate,
                notes = notes,
                userId = currentUser
            )
            transactionsList.add(newTransaction)

            Log.d(TAG, "Created new transaction ID: ${newTransaction.id}")
        }

        saveTransactionsToPrefs()

        setResult(RESULT_OK)
        finish()
    }

    private fun deleteTransaction() {
        if (isEditMode) {
            transactionsList.removeIf { it.id == currentTransactionId }

            saveTransactionsToPrefs()

            Log.d(TAG, "Deleted transaction ID: $currentTransactionId")

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

        Log.d(TAG, "Saved ${transactionsList.size} transactions to SharedPreferences")
    }
}