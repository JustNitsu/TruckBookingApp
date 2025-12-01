package com.example.truckbooking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Wait for 2.5 seconds (2500ms), then go to Login Screen
        Handler(Looper.getMainLooper()).postDelayed({
            // CHECK SESSION
            val prefs = getSharedPreferences("TruckApp", MODE_PRIVATE)
            val isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false)

            if(isLoggedIn) {
                val name = prefs.getString("NAME", "")
                val role = prefs.getString("ROLE", "")
                val intent = Intent(this, DashboardActivity::class.java)
                intent.putExtra("NAME", name)
                intent.putExtra("ROLE", role)
                startActivity(intent)
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 2500)
    }
}