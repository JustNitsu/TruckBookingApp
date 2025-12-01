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

        // REMOVED "Admin" from this list. Only User and Owner can sign up.
        val roles = arrayOf("User", "Owner")

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
    }

    private fun setupRegisterButton() {
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etUser = findViewById<EditText>(R.id.etRegUser)
        val etPass = findViewById<EditText>(R.id.etRegPass)
        val etPhone = findViewById<EditText>(R.id.etRegPhone) // Ensure this exists in XML
        val spinner = findViewById<Spinner>(R.id.spinnerRole)

        btnRegister.setOnClickListener {
            val username = etUser.text.toString().trim()
            val password = etPass.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val role = spinner.selectedItem.toString()

            if (validateInput(username, password, phone)) {
                saveUserToDatabase(username, password, role, phone)
            }
        }
    }

    private fun validateInput(user: String, pass: String, phone: String): Boolean {
        if (user.isEmpty() || pass.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 4) {
            Toast.makeText(this, "Password is too short (min 4 chars)", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveUserToDatabase(user: String, pass: String, role: String, phone: String) {
        val success = db.insertUser(user, pass, role, phone)
        if (success) {
            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Username already exists. Try another.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLoginLink() {
        val tvLogin = findViewById<TextView>(R.id.tvGoToLogin)
        tvLogin.setOnClickListener {
            finish()
        }
    }
}