package com.stripe.example

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // 0 - for private mode
            // Create intent to start new Activity
            // Create intent to start new Activity
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)

            finish()
        }, 5000)
    }
}