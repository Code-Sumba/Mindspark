package com.umang.MindzSpark.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.umang.MindzSpark.R
import com.umang.MindzSpark.auth.AuthenticationActivity
import com.umang.MindzSpark.databinding.ActivityGettingStartedBinding
import com.umang.MindzSpark.general.HomeActivity

class GettingStartedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGettingStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityGettingStartedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the button click listener
        binding.btnGetStarted.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
