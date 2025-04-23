package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.os.Handler
import android.os.Looper

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"

    // UI Elements
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserFullName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etCurrency: TextInputEditText
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnSaveProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var btnBack: ImageView

    // User data
    private var userEmail = ""
    private var userName = ""
    private var userPhone = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        try {
            // Initialize views
            initViews()

            // Load user data
            loadUserData()

            // Set up click listeners
            setupClickListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ProfileActivity", e)
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        tvUserFullName = findViewById(R.id.tvUserFullName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etCurrency = findViewById(R.id.etCurrency)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadUserData() {
        try {
            // Get current user email from shared preferences
            val sharedPrefs = getSharedPreferences("finflow_session", MODE_PRIVATE)
            userEmail = sharedPrefs.getString("logged_in_email", "") ?: ""

            if (userEmail.isEmpty()) {
                Log.e(TAG, "No user logged in")
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                navigateToLogin()
                return
            }

            // Load user data from profile preferences
            val profilePrefs = getSharedPreferences("finflow_profiles", MODE_PRIVATE)
            userName = profilePrefs.getString("${userEmail}_name", "") ?: ""
            userPhone = profilePrefs.getString("${userEmail}_phone", "") ?: ""

            // Display user data
            tvUserFullName.text = userName
            tvUserEmail.text = userEmail
            etFullName.setText(userName)
            etEmail.setText(userEmail)
            etPhone.setText(userPhone)

        } catch (e: Exception) {
            Log.e(TAG, "Error loading user data", e)
        }
    }

    private fun setupClickListeners() {
        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Edit Profile button
        btnEditProfile.setOnClickListener {
            toggleEditMode(true)
        }

        // Save Profile button
        btnSaveProfile.setOnClickListener {
            saveProfileChanges()
            toggleEditMode(false)
        }

        // Change Password button
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout button
        btnLogout.setOnClickListener {
            confirmLogout()
        }

        // Delete Account button
        btnDeleteAccount.setOnClickListener {
            confirmDeleteAccount()
        }
    }

    private fun toggleEditMode(edit: Boolean) {
        isEditMode = edit

        // Enable/disable fields
        etFullName.isEnabled = edit
        etPhone.isEnabled = edit

        // Show/hide appropriate buttons
        btnEditProfile.visibility = if (edit) View.GONE else View.VISIBLE
        btnSaveProfile.visibility = if (edit) View.VISIBLE else View.GONE

        // Add focus and selection if entering edit mode
        if (edit) {
            etFullName.requestFocus()
            etFullName.setSelection(etFullName.text?.length ?: 0)
        }
    }

    private fun saveProfileChanges() {
        try {
            val newName = etFullName.text.toString().trim()
            val newPhone = etPhone.text.toString().trim()

            // Validate inputs
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            // Update shared preferences
            val profilePrefs = getSharedPreferences("finflow_profiles", MODE_PRIVATE)
            val editor = profilePrefs.edit()

            editor.putString("${userEmail}_name", newName)
            editor.putString("${userEmail}_phone", newPhone)
            editor.apply()

            // Update UI
            userName = newName
            userPhone = newPhone
            tvUserFullName.text = userName

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile changes", e)
            Toast.makeText(this, "Failed to save profile changes", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        try {
            // Create custom dialog layout
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)

            // Get references to EditText fields
            val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
            val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
            val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

            if (etCurrentPassword == null || etNewPassword == null || etConfirmPassword == null) {
                Log.e(TAG, "Failed to find password input fields in dialog layout")
                Toast.makeText(this, "Error showing change password dialog", Toast.LENGTH_SHORT).show()
                return
            }

            // Create the AlertDialog
            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton("Change", null) // Setting null here so we can override later
                .setNegativeButton("Cancel", null)
                .create()

            // Show the dialog
            alertDialog.show()

            // Set a click listener for the positive button after dialog is shown
            // This prevents automatic dismissal when validation fails
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val currentPassword = etCurrentPassword.text.toString().trim()
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                // Basic validation
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Verify current password
                val profilePrefs = getSharedPreferences("finflow_profiles", MODE_PRIVATE)
                val storedPassword = profilePrefs.getString("${userEmail}_password", "")

                Log.d(TAG, "Checking password for user: $userEmail")
                Log.d(TAG, "Entered current password: $currentPassword")
                Log.d(TAG, "Stored password: $storedPassword")

                if (storedPassword != currentPassword) {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Update password
                try {
                    val editor = profilePrefs.edit()
                    editor.putString("${userEmail}_password", newPassword)
                    editor.apply()

                    Log.d(TAG, "Password updated successfully for user: $userEmail")
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating password", e)
                    Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing change password dialog", e)
            Toast.makeText(this, "Failed to show dialog", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        try {
            // Clear logged in user from session
            val sessionPrefs = getSharedPreferences("finflow_session", MODE_PRIVATE)
            val editor = sessionPrefs.edit()
            editor.clear()
            editor.apply()

            // Navigate to login page
            navigateToLogin()

            // Show toast
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Toast.makeText(this, "Failed to logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDeleteAccount() {
        try {
            Log.d(TAG, "Attempting to delete account for user: $userEmail")

            // First verify that we have a valid user email
            if (userEmail.isEmpty()) {
                Log.e(TAG, "Cannot delete account - no user email available")
                Toast.makeText(this, "Error deleting account: user information missing", Toast.LENGTH_SHORT).show()
                return
            }

            // Delete user data from profile preferences
            val profilePrefs = getSharedPreferences("finflow_profiles", MODE_PRIVATE)
            val editor = profilePrefs.edit()

            // Remove all user-related data
            editor.remove("${userEmail}_name")
            editor.remove("${userEmail}_password")
            editor.remove("${userEmail}_phone")

            // Also remove any other possible data fields
            editor.remove("${userEmail}_currency")
            editor.remove("${userEmail}_profile_pic")

            // Apply changes
            val result = editor.commit() // Use commit() instead of apply() to get immediate result

            if (!result) {
                Log.e(TAG, "Failed to commit changes to SharedPreferences")
                Toast.makeText(this, "Error deleting account data", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "User data deleted from preferences successfully")

            // Clear session
            val sessionPrefs = getSharedPreferences("finflow_session", MODE_PRIVATE)
            val sessionEditor = sessionPrefs.edit()
            sessionEditor.clear()
            val sessionResult = sessionEditor.commit()

            if (!sessionResult) {
                Log.e(TAG, "Failed to clear session data")
            }

            // Success toast
            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login - using a slight delay to ensure toast is visible
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToLogin()
            }, 1000)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account", e)
            Toast.makeText(this, "Failed to delete account: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to login", e)
            // If navigation fails, try to force close the app as fallback
            finishAffinity()
        }
    }
}