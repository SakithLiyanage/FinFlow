package com.example.finflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton

class OnboardingActivity : AppCompatActivity() {

    private lateinit var onboardingViewPager: ViewPager2
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var btnGetStarted: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)

        onboardingViewPager = findViewById(R.id.onboardingViewPager)
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        btnGetStarted = findViewById(R.id.btnGetStarted)

        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.onboarding_image1,
                "Track Your Expenses",
                "Monitor your daily expenses and stay on top of your finances with real-time tracking."
            ),
            OnboardingItem(
                R.drawable.onboarding_image2,
                "Smart Budgeting",
                "Create personalized budgets and get insights on your spending habits to save more."
            ),
            OnboardingItem(
                R.drawable.onboarding_image3,
                "Financial Goals",
                "Set and achieve your financial goals with detailed progress tracking and visualizations."
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        onboardingViewPager.adapter = adapter

        setupIndicators()
        setCurrentIndicator(0)

        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                if (position == onboardingItems.size - 1) {
                    btnNext.visibility = View.INVISIBLE
                    btnSkip.visibility = View.INVISIBLE
                    btnGetStarted.visibility = View.VISIBLE
                } else {
                    btnNext.visibility = View.VISIBLE
                    btnSkip.visibility = View.VISIBLE
                    btnGetStarted.visibility = View.INVISIBLE
                }
            }
        })

        btnNext.setOnClickListener {
            if (onboardingViewPager.currentItem < onboardingItems.size - 1) {
                onboardingViewPager.currentItem += 1
            }
        }

        btnSkip.setOnClickListener {
            navigateToWelcomeScreen()
        }

        btnGetStarted.setOnClickListener {
            navigateToWelcomeScreen()
        }
    }

    private fun navigateToWelcomeScreen() {
        val sharedPreferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_onboarding_completed", true).apply()

        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(3)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            indicatorsContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
}