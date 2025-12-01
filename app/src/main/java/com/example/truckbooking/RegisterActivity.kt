package com.example.truckbooking

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        setupRoleSpinner()
        setupRegisterButton()
        setupLoginLink()
    }

    private fun setupRoleSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerRole)
        // We only allow User and Owner. "Admin" is removed as requested.
        val roles = arrayOf("User", "Owner")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
    }

    private fun setupRegisterButton() {
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etUser = findViewById<EditText>(R.id.etRegUser)
        val etPass = findViewById<EditText>(R.id.etRegPass)
        val spinner = findViewById<Spinner>(R.id.spinnerRole)

        btnRegister.setOnClickListener {
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()
            val role = spinner.selectedItem.toString()

            if (validateInput(username, password)) {
                saveUserToDatabase(username, password, role)
            }
        }
    }

    private fun validateInput(user: String, pass: String): Boolean {
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 4) {
            Toast.makeText(this, "Password is too short (min 4 chars)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserToDatabase(user: String, pass: String, role: String) {
        val success = db.insertUser(user, pass, role)
        if (success) {
            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_LONG).show()
            finish() // Close this screen and return to Login
        } else {
            Toast.makeText(this, "Username already exists. Try another.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLoginLink() {
        val tvLogin = findViewById<TextView>(R.id.tvGoToLogin)
        tvLogin.setOnClickListener {
            finish() // Simply closes this activity to reveal the Login screen behind it
        }
    }
}