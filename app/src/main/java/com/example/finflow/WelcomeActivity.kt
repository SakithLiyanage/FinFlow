package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {

    private lateinit var welcomeContainer: ConstraintLayout
    private lateinit var logoCard: CardView
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnSignUp: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Make status bar transparent with light icons
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // Initialize views
        welcomeContainer = findViewById(R.id.welcomeContainer)
        logoCard = findViewById(R.id.logoCard)
        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        // Apply animations
        runAnimations()

        // Set click listeners
        btnLogin.setOnClickListener {
            // Navigate to Login Screen
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnSignUp.setOnClickListener {
            // Navigate to Sign Up Screen
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun runAnimations() {
        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeIn.duration = 1000

        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        slideUp.duration = 800

        // Apply animations with delay
        logoCard.startAnimation(fadeIn)

        // Add slight delay to buttons animation
        btnLogin.alpha = 0f
        btnSignUp.alpha = 0f

        btnLogin.animate()
            .alpha(1f)
            .translationYBy(-50f)
            .setDuration(800)
            .setStartDelay(300)
            .start()

        btnSignUp.animate()
            .alpha(1f)
            .translationYBy(-50f)
            .setDuration(800)
            .setStartDelay(500)
            .start()
    }
}