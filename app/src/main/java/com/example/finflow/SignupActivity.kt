package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity() {

    private lateinit var fullNameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var etFullName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnSignUpSubmit: MaterialButton
    private lateinit var tvLogin: TextView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Make status bar transparent with light icons
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        // Initialize views
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignUpSubmit = findViewById(R.id.btnSignUpSubmit)
        tvLogin = findViewById(R.id.tvLogin)
        btnBack = findViewById(R.id.btnBack)

        // Set up input validation watchers
        setupInputValidation()

        // Set click listeners
        btnBack.setOnClickListener {
            onBackPressed()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        btnSignUpSubmit.setOnClickListener {
            validateAndCreateAccount()
        }
    }

    private fun setupInputValidation() {
        etFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    fullNameInputLayout.error = null
                }
            }
        })

        etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    emailInputLayout.error = null
                }
            }
        })

        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    passwordInputLayout.error = null
                } else if (s.toString().length < 8) {
                    passwordInputLayout.helperText = "Password must be at least 8 characters"
                } else {
                    passwordInputLayout.helperText = null
                }
            }
        })
    }

    private fun validateAndCreateAccount() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true

        // Validate fullName
        if (fullName.isEmpty()) {
            fullNameInputLayout.error = "Name is required"
            isValid = false
        }

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!isValidEmail(email)) {
            emailInputLayout.error = "Enter a valid email address"
            isValid = false
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordInputLayout.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (isValid) {
            // Check if user already exists
            val sharedPreferences = getSharedPreferences("finflow_users", MODE_PRIVATE)
            if (sharedPreferences.contains(email)) {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "An account with this email already exists",
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                // Save user data
                val editor = sharedPreferences.edit()
                editor.putString(email, password)
                editor.apply()

                // Save user profile data
                val profilePref = getSharedPreferences("finflow_profiles", MODE_PRIVATE)
                val profileEditor = profilePref.edit()
                profileEditor.putString("${email}_name", fullName)
                profileEditor.putString("${email}_created", System.currentTimeMillis().toString())
                profileEditor.apply()

                // Create a login session
                val sessionPref = getSharedPreferences("finflow_session", MODE_PRIVATE)
                val sessionEditor = sessionPref.edit()
                sessionEditor.putBoolean("is_logged_in", true)
                sessionEditor.putString("logged_in_email", email)
                sessionEditor.apply()

                // Show animation and feedback
                btnSignUpSubmit.startAnimation()

                // Navigate to Home Activity with a slight delay for animation
                android.os.Handler().postDelayed({
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                }, 1500)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    // Extension function for button loading animation
    private fun MaterialButton.startAnimation() {
        this.isEnabled = false
        this.text = "Creating Account..."
        this.icon = ContextCompat.getDrawable(context, R.drawable.ic_loading)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}