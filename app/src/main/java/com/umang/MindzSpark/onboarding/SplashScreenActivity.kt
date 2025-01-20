package com.umang.MindzSpark.onboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.umang.MindzSpark.R
import com.umang.MindzSpark.databinding.ActivitySplashScreenBinding
import com.umang.MindzSpark.general.HomeActivity
import com.umang.MindzSpark.utils.AppPreferences

class SplashScreenActivity : AppCompatActivity() {

    private val splashDelay: Long = 3000 // 3 seconds
    private lateinit var binding: ActivitySplashScreenBinding
    private var progressBarStatus = 0
    private var dummy = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize app preferences
        AppPreferences.init(this)

        // Start the progress bar update thread
        Thread {
            while (progressBarStatus < 100) {
                try {
                    dummy += 25
                    Thread.sleep(100) // Simulate some work
                    progressBarStatus = dummy

                    // Update progress bar on the main thread
                    handler.post {
                        binding.splashScreenProgressBar.progress = progressBarStatus
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            // Navigate to the next activity based on login status
            if (AppPreferences.isLogin) {
                navigateToHome()
            } else {
                navigateToGettingStarted()
            }
        }.start()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToGettingStarted() {
        val intent = Intent(this, GettingStartedActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
