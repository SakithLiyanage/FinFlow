package com.example.finflow

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

import android.widget.FrameLayout
import com.example.finflow.utils.PermissionHelper


class ReportsActivity : AppCompatActivity() {

    private val TAG = "ReportsActivity"
    // Set the timestamp and user
    private val currentDateTime = "2025-04-24 06:52:42"
    private val currentUser = "SakithLiyanage"

    private lateinit var btnBack: ImageView
    private lateinit var btnFilterDate: MaterialButton
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvNetBalance: TextView
    private lateinit var rvCategorySummary: RecyclerView
    private lateinit var btnExportJson: MaterialButton
    private lateinit var btnExportText: MaterialButton
    private lateinit var btnImportData: MaterialButton
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var pieChart: PieChart

    private var transactionsList = ArrayList<Transaction>()
    private var filteredTransactions = ArrayList<Transaction>()
    private var categorySummaryList = ArrayList<CategorySummary>()
    private lateinit var categorySummaryAdapter: CategorySummaryAdapter

    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    // Filter period
    private val calendar = Calendar.getInstance()
    private val yearMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var filterType = FilterType.MONTH // Default to monthly view

    // Date picker
    private val startOfMonth: Calendar
        get() {
            val cal = calendar.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal
        }

    private val endOfMonth: Calendar
        get() {
            val cal = calendar.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        Log.d(TAG, "Analytics activity started at $currentDateTime by $currentUser")

        initViews()
        setupChartView()
        registerFilePicker()
        setupNavigation()
        setupListeners()

        loadTransactions()
        filterTransactions()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnFilterDate = findViewById(R.id.btnFilterDate)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvNetBalance = findViewById(R.id.tvNetBalance)
        rvCategorySummary = findViewById(R.id.rvCategorySummary)
        btnExportJson = findViewById(R.id.btnExportJson)
        btnExportText = findViewById(R.id.btnExportText)
        btnImportData = findViewById(R.id.btnImportData)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Find the chart container and add the PieChart
        val chartContainer = findViewById<View>(R.id.chartContainer)
        pieChart = PieChart(this)
        (chartContainer as android.widget.FrameLayout).addView(pieChart)

        // Setup RecyclerView
        categorySummaryAdapter = CategorySummaryAdapter(categorySummaryList)
        rvCategorySummary.layoutManager = LinearLayoutManager(this)
        rvCategorySummary.adapter = categorySummaryAdapter
        rvCategorySummary.isNestedScrollingEnabled = false

        // Initialize filter date button text
        updateFilterButtonText()
    }

    private fun setupChartView() {
        // Configure pie chart
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 40f
            holeRadius = 35f
            setUsePercentValues(true)
            legend.isEnabled = true
            setDrawEntryLabels(false)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
        }
    }

    private fun registerFilePicker() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    importData(inputStream)
                    Log.d(TAG, "File picked at $currentDateTime by $currentUser")
                } catch (e: Exception) {
                    Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Import failed", e)
                }
            }
        }
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
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetsActivity::class.java))
                    true
                }
                R.id.nav_reports -> true
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.nav_reports
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnFilterDate.setOnClickListener {
            showDateFilterDialog()
        }

        btnExportJson.setOnClickListener {
            if (PermissionHelper.checkStoragePermissions(this)) {
                exportDataAsJson()
            } else {
                Toast.makeText(this, "Storage permission required to export files", Toast.LENGTH_LONG).show()
            }
        }

        btnExportText.setOnClickListener {
            if (PermissionHelper.checkStoragePermissions(this)) {
                exportDataAsText()
            } else {
                Toast.makeText(this, "Storage permission required to export files", Toast.LENGTH_LONG).show()
            }
        }

        btnImportData.setOnClickListener {
            showImportOptions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (PermissionHelper.handlePermissionResult(requestCode, grantResults)) {
            // Permission was granted
            when (requestCode) {
                101 -> {
                    Toast.makeText(this, "Permission granted! You can now export files.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Permission was denied
            Toast.makeText(this, "Storage permission is required to save files", Toast.LENGTH_LONG).show()
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

    private fun filterTransactions() {
        val startDate = startOfMonth
        val endDate = endOfMonth

        // Filter transactions by date range
        filteredTransactions = when (filterType) {
            FilterType.MONTH -> {
                transactionsList.filter {
                    it.date.time >= startDate.timeInMillis && it.date.time <= endDate.timeInMillis
                } as ArrayList<Transaction>
            }
            FilterType.YEAR -> {
                val year = calendar.get(Calendar.YEAR)
                transactionsList.filter {
                    val cal = Calendar.getInstance()
                    cal.time = it.date
                    cal.get(Calendar.YEAR) == year
                } as ArrayList<Transaction>
            }
            FilterType.ALL_TIME -> {
                ArrayList(transactionsList)
            }
        }

        calculateSummary()
        generateCategorySummary()
        updatePieChart()

        Log.d(TAG, "Filtered ${filteredTransactions.size} transactions for period: ${btnFilterDate.text}")
    }

    private fun calculateSummary() {
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in filteredTransactions) {
            if (transaction.type == TransactionType.INCOME) {
                totalIncome += transaction.amount
            } else {
                totalExpense += transaction.amount
            }
        }

        val netBalance = totalIncome - totalExpense

        tvTotalIncome.text = String.format("LKR %.2f", totalIncome)
        tvTotalExpenses.text = String.format("LKR %.2f", totalExpense)
        tvNetBalance.text = String.format("LKR %.2f", netBalance)
    }

    private fun generateCategorySummary() {
        categorySummaryList.clear()

        // Group transactions by category
        val categoryMap = HashMap<String, Double>()
        var totalExpenses = 0.0

        // Only consider expenses for category breakdown
        for (transaction in filteredTransactions) {
            if (transaction.type == TransactionType.EXPENSE) {
                val category = transaction.category
                val currentAmount = categoryMap[category] ?: 0.0
                categoryMap[category] = currentAmount + transaction.amount
                totalExpenses += transaction.amount
            }
        }

        // Create category summary objects
        for ((category, amount) in categoryMap) {
            val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100).toInt() else 0
            categorySummaryList.add(CategorySummary(category, amount, percentage))
        }

        // Sort by amount (highest first)
        categorySummaryList.sortByDescending { it.amount }

        // Update the adapter
        categorySummaryAdapter.updateData(categorySummaryList)
    }

    private fun updatePieChart() {
        // Create pie chart entries for Income vs Expense (not categories)
        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // Calculate total income and expense
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in filteredTransactions) {
            if (transaction.type == TransactionType.INCOME) {
                totalIncome += transaction.amount
            } else {
                totalExpense += transaction.amount
            }
        }

        val total = totalIncome + totalExpense

        // Create entries for Income and Expense with their percentages
        if (total > 0) {
            val incomePercentage = (totalIncome / total * 100).toFloat()
            val expensePercentage = (totalExpense / total * 100).toFloat()

            // Only add entries if there are values
            if (totalIncome > 0) {
                entries.add(PieEntry(incomePercentage, "Income"))
                colors.add(ContextCompat.getColor(this, R.color.income_green))
            }

            if (totalExpense > 0) {
                entries.add(PieEntry(expensePercentage, "Expenses"))
                colors.add(ContextCompat.getColor(this, R.color.expense_red))
            }
        }

        // Add placeholder if no data
        if (entries.isEmpty()) {
            entries.add(PieEntry(1f, "No Data"))
            colors.add(Color.LTGRAY)
        }

        // Configure the dataset
        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        // Set value formatting
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.WHITE)

        // Update chart
        pieChart.data = pieData
        pieChart.invalidate()
        pieChart.animateY(1000)

        Log.d(TAG, "Updated Income vs Expense chart - Income: $totalIncome, Expense: $totalExpense")
    }

    private fun showDateFilterDialog() {
        val options = arrayOf("This Month", "This Year", "All Time", "Custom Range")

        AlertDialog.Builder(this)
            .setTitle("Select Date Range")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // This Month
                        filterType = FilterType.MONTH
                        calendar.timeInMillis = System.currentTimeMillis()
                        updateFilterButtonText()
                        filterTransactions()
                    }
                    1 -> { // This Year
                        filterType = FilterType.YEAR
                        calendar.timeInMillis = System.currentTimeMillis()
                        updateFilterButtonText()
                        filterTransactions()
                    }
                    2 -> { // All Time
                        filterType = FilterType.ALL_TIME
                        updateFilterButtonText()
                        filteredTransactions = ArrayList(transactionsList)
                        calculateSummary()
                        generateCategorySummary()
                        updatePieChart()
                    }
                    3 -> { // Custom Range
                        showMonthYearPicker()
                    }
                }
            }
            .show()
    }

    private fun showMonthYearPicker() {
        val monthYearPicker = DatePickerDialog(
            this,
            { _, year, month, _ ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                filterType = FilterType.MONTH
                updateFilterButtonText()
                filterTransactions()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Try to hide the day picker if possible
        try {
            val datePickerField = monthYearPicker.javaClass.getDeclaredField("mDatePicker")
            datePickerField.isAccessible = true
            val datePicker = datePickerField.get(monthYearPicker) as android.widget.DatePicker
            val dayField = datePicker.javaClass.getDeclaredField("mDaySpinner")
            dayField.isAccessible = true
            val daySpinner = dayField.get(datePicker) as View
            daySpinner.visibility = View.GONE
        } catch (e: Exception) {
            e.printStackTrace()
        }

        monthYearPicker.show()
    }

    private fun updateFilterButtonText() {
        btnFilterDate.text = when (filterType) {
            FilterType.MONTH -> yearMonthFormat.format(calendar.time)
            FilterType.YEAR -> "${calendar.get(Calendar.YEAR)}"
            FilterType.ALL_TIME -> "All Time"
        }
    }

    private fun exportDataAsJson() {
        try {
            // Create JSON content
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(transactionsList)

            // First save to shared external storage (public Documents)
            val publicDocumentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val finflowDir = File(publicDocumentsDir, "FinFlow")
            if (!finflowDir.exists()) {
                finflowDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "finflow_export_$timestamp.json"
            val publicFile = File(finflowDir, fileName)

            // Write to public storage
            FileOutputStream(publicFile).use {
                it.write(jsonString.toByteArray())
            }

            // Also save to app's private storage for sharing via FileProvider
            val privateFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(privateFile).use {
                it.write(jsonString.toByteArray())
            }

            // Share the app's private copy
            shareFile(privateFile, "application/json")

            Toast.makeText(this, "Exported to Documents/FinFlow/$fileName", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Exported ${transactionsList.size} transactions to JSON at 2025-04-24 08:04:27 by SakithLiyanage")
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "JSON export failed", e)
            e.printStackTrace()
        }
    }

    private fun exportDataAsText() {
        try {
            // Create text content
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val stringBuilder = StringBuilder()

            stringBuilder.appendLine("FinFlow Transactions Export")
            stringBuilder.appendLine("Generated on 2025-04-24 08:04:27 by SakithLiyanage")
            stringBuilder.appendLine("---------------------------------------")

            for (transaction in transactionsList) {
                val typeStr = if (transaction.type == TransactionType.INCOME) "INCOME" else "EXPENSE"
                stringBuilder.appendLine("Date: ${formatter.format(transaction.date)}")
                stringBuilder.appendLine("Type: $typeStr")
                stringBuilder.appendLine("Title: ${transaction.title}")
                stringBuilder.appendLine("Amount: LKR ${transaction.amount}")
                stringBuilder.appendLine("Category: ${transaction.category}")
                if (transaction.notes.isNotBlank()) {
                    stringBuilder.appendLine("Notes: ${transaction.notes}")
                }
                stringBuilder.appendLine("---------------------------------------")
            }

            // First save to shared external storage (public Documents)
            val publicDocumentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val finflowDir = File(publicDocumentsDir, "FinFlow")
            if (!finflowDir.exists()) {
                finflowDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "finflow_export_$timestamp.txt"
            val publicFile = File(finflowDir, fileName)

            // Write to public storage
            FileOutputStream(publicFile).use {
                it.write(stringBuilder.toString().toByteArray())
            }

            // Also save to app's private storage for sharing via FileProvider
            val privateFile = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(privateFile).use {
                it.write(stringBuilder.toString().toByteArray())
            }

            // Share the app's private copy
            shareFile(privateFile, "text/plain")

            Toast.makeText(this, "Exported to Documents/FinFlow/$fileName", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Exported ${transactionsList.size} transactions to text at 2025-04-24 08:04:27 by SakithLiyanage")
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Text export failed", e)
            e.printStackTrace()
        }
    }

    private fun showImportOptions() {
        val options = arrayOf("Import from JSON Backup", "Import from Text Backup")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Data")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> filePickerLauncher.launch("application/json")
                    1 -> filePickerLauncher.launch("text/plain")
                }
            }
            .show()
    }

    private fun importData(inputStream: InputStream?) {
        if (inputStream == null) {
            Toast.makeText(this, "Could not read the file", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()

            // Try to parse as JSON
            val listType = object : TypeToken<ArrayList<Transaction>>() {}.type
            val importedTransactions = Gson().fromJson<ArrayList<Transaction>>(content, listType)

            if (importedTransactions != null && importedTransactions.isNotEmpty()) {
                showConfirmationDialog(importedTransactions)
            } else {
                Toast.makeText(this, "No valid transactions found in the file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not parse the file. Please ensure it's a valid FinFlow backup.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Import parsing failed", e)
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing stream", e)
            }
        }
    }

    private fun showConfirmationDialog(importedTransactions: ArrayList<Transaction>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Import Confirmation")
            .setMessage("Found ${importedTransactions.size} transactions. How would you like to import them?")
            .setPositiveButton("Replace All") { _, _ ->
                // Replace current data with imported data
                transactionsList = importedTransactions
                saveTransactionsToPrefs()
                filterTransactions() // Reload UI
                Toast.makeText(this, "Imported ${importedTransactions.size} transactions", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Replaced all transactions with ${importedTransactions.size} imported items at $currentDateTime by $currentUser")
            }
            .setNeutralButton("Add to Existing") { _, _ ->
                // Add imported data to current data (avoiding duplicates by ID)
                val existingIds = transactionsList.map { it.id }.toSet()
                val newTransactions = importedTransactions.filter { it.id !in existingIds }
                transactionsList.addAll(newTransactions)
                saveTransactionsToPrefs()
                filterTransactions() // Reload UI
                Toast.makeText(this, "Added ${newTransactions.size} new transactions", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Added ${newTransactions.size} new transactions to existing data at $currentDateTime by $currentUser")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun saveTransactionsToPrefs() {
        val sharedPrefs = getSharedPreferences("finflow_transactions", MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        val transactionsJson = Gson().toJson(transactionsList)
        editor.putString("transactions_data", transactionsJson)
        editor.apply()
        Log.d(TAG, "Saved ${transactionsList.size} transactions to SharedPreferences at $currentDateTime by $currentUser")
    }

    private fun shareFile(file: File, mimeType: String) {
        try {
            val fileUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider", // Make sure this matches the authority in AndroidManifest.xml
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Sharing failed: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e(TAG, "File sharing failed", e)
            e.printStackTrace()
        }
    }
}

enum class FilterType {
    MONTH, YEAR, ALL_TIME
}

// Model class for category summary items
data class CategorySummary(
    val category: String,
    val amount: Double,
    val percentage: Int
)

// Adapter class for RecyclerView
// Make sure this code is in ReportsActivity.kt
class CategorySummaryAdapter(private var items: List<CategorySummary>) :
    RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Simplify the ViewHolder to just reference the views we definitely have
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val progressBar: View = view.findViewById(R.id.progressBar)
        val tvPercentage: TextView = view.findViewById(R.id.tvPercentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Set text data
        holder.tvCategory.text = item.category
        holder.tvAmount.text = String.format("LKR %.2f", item.amount)
        holder.tvPercentage.text = "${item.percentage}%"

        // Fix for progress bar not showing - use post to ensure the view is measured
        holder.itemView.post {
            val parentWidth = holder.itemView.findViewById<FrameLayout>(R.id.progressBarContainer).width
            val progressWidth = (item.percentage * parentWidth) / 100

            // Ensure minimum width for visibility
            val finalWidth = maxOf(progressWidth, 4).toInt()

            val layoutParams = holder.progressBar.layoutParams
            layoutParams.width = finalWidth
            holder.progressBar.layoutParams = layoutParams
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CategorySummary>) {
        items = newItems
        notifyDataSetChanged()
    }
}