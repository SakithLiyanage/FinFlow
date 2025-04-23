package com.example.finflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val onboardingItems: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size
    }

    inner class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageOnboarding = view.findViewById<ImageView>(R.id.imageOnboarding)
        private val textTitle = view.findViewById<TextView>(R.id.textTitle)
        private val textDescription = view.findViewById<TextView>(R.id.textDescription)

        fun bind(onboardingItem: OnboardingItem) {
            imageOnboarding.setImageResource(onboardingItem.image)
            textTitle.text = onboardingItem.title
            textDescription.text = onboardingItem.description

            // Apply animations
            imageOnboarding.alpha = 0f
            textTitle.alpha = 0f
            textDescription.alpha = 0f

            imageOnboarding.translationY = 50f
            textTitle.translationY = 50f
            textDescription.translationY = 50f

            imageOnboarding.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(300).start()
            textTitle.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(500).start()
            textDescription.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(700).start()
        }
    }
}