package com.dcelysia.outsourceserviceproject.Activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dcelysia.outsourceserviceproject.R

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var monthlyTab: TextView
    private lateinit var yearlyTab: TextView
    private lateinit var planViewFlipper: ViewFlipper
    private lateinit var monthlyPayButton: TextView
    private lateinit var yearlyPayButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        // Initialize views
        backButton = findViewById(R.id.backButton)
        monthlyTab = findViewById(R.id.monthlyTab)
        yearlyTab = findViewById(R.id.yearlyTab)
        planViewFlipper = findViewById(R.id.planViewFlipper)
        monthlyPayButton = findViewById(R.id.monthlyPayButton)
        yearlyPayButton = findViewById(R.id.yearlyPayButton)

        // Set initial state - Monthly tab is selected by default
        showMonthlyPlan()

        // Set click listeners
        backButton.setOnClickListener { onBackPressed() }

        monthlyTab.setOnClickListener { showMonthlyPlan() }

        yearlyTab.setOnClickListener { showYearlyPlan() }

        monthlyPayButton.setOnClickListener {
            Toast.makeText(this, "Processing monthly payment of ¥25.9", Toast.LENGTH_SHORT).show()
            // TODO: Implement payment processing logic
        }

        yearlyPayButton.setOnClickListener {
            Toast.makeText(this, "Processing yearly payment of ¥188", Toast.LENGTH_SHORT).show()
            // TODO: Implement payment processing logic
        }
    }

    private fun showMonthlyPlan() {
        // Update tab appearance
        monthlyTab.background = ContextCompat.getDrawable(this, R.drawable.tab_selected_background)
        monthlyTab.setTextColor(ContextCompat.getColor(this, R.color.white))

        yearlyTab.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected_background)
        yearlyTab.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Show monthly plan content
        planViewFlipper.displayedChild = 0
    }

    private fun showYearlyPlan() {
        // Update tab appearance
        yearlyTab.background = ContextCompat.getDrawable(this, R.drawable.tab_selected_background)
        yearlyTab.setTextColor(ContextCompat.getColor(this, R.color.white))

        monthlyTab.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected_background)
        monthlyTab.setTextColor(ContextCompat.getColor(this, R.color.black))

        // Show yearly plan content
        planViewFlipper.displayedChild = 1
    }
}