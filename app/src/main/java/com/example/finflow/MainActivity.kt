package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val splashDuration: Long = 3000 // 3 seconds
    private lateinit var ivLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var tvTagline: TextView
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide system UI (full screen)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // Initialize views
        ivLogo = findViewById(R.id.ivLogo)
        tvAppName = findViewById(R.id.tvAppName)
        tvTagline = findViewById(R.id.tvTagline)
        progressBar = findViewById(R.id.progressBar)

        // Start animations
        startAnimations()

        // Navigate to next activity after animations
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@MainActivity, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDuration)
    }

    private fun startAnimations() {
        // Logo animations
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Combine animations for logo
        val logoAnimSet = AnimationSet(true)
        logoAnimSet.addAnimation(scaleAnimation)
        logoAnimSet.addAnimation(fadeAnimation)
        ivLogo.startAnimation(logoAnimSet)

        // Text animations
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        slideUp.startOffset = 400
        tvAppName.startAnimation(slideUp)

        val slideUpTagline = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        slideUpTagline.startOffset = 600
        tvTagline.startAnimation(slideUpTagline)

        // Progress bar animations
        progressBar.scaleX = 0f
        progressBar.animate()
            .scaleX(1f)
            .setDuration(2000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }
    private fun checkOnboardingStatus() {
        val sharedPreferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE)
        val isOnboardingCompleted = sharedPreferences.getBoolean("is_onboarding_completed", false)

        if (isOnboardingCompleted) {
            // User has completed onboarding, go to home
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            // User needs to see onboarding
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        finish()
    }
}