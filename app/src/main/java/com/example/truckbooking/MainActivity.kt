package com.example.truckbooking

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Database
        db = DatabaseHelper(this)

        // Setup Buttons
        setupLoginButton()
        setupRegisterLink()
    }

    private fun setupLoginButton() {
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val etUser = findViewById<EditText>(R.id.etLoginUser)
        val etPass = findViewById<EditText>(R.id.etLoginPass)

        btnLogin.setOnClickListener {
            val username = etUser.text.toString().trim() // .trim() removes accidental spaces
            val password = etPass.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter Username and Password", Toast.LENGTH_SHORT).show()
            } else {
                performLogin(username, password)
            }
        }
    }

    private fun performLogin(user: String, pass: String) {
        val role = db.checkLogin(user, pass)

        if (role != null) {
            // Login Success
            Toast.makeText(this, "Welcome back, $user!", Toast.LENGTH_SHORT).show()
            navigateToDashboard(user, role)
        } else {
            // Login Failed
            Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard(username: String, role: String) {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("NAME", username)
        intent.putExtra("ROLE", role)
        startActivity(intent)
        finish() // Prevents user from going back to login screen using 'Back' button
    }

    private fun setupRegisterLink() {
        val tvRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}