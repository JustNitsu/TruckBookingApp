package com.example.truckbooking

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = DatabaseHelper(this)
        val username = intent.getStringExtra("NAME")

        val etPass = findViewById<EditText>(R.id.etNewPass)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnSave.setOnClickListener {
            val newPass = etPass.text.toString()
            if (newPass.isNotEmpty() && username != null) {
                db.updatePassword(username, newPass)
                Toast.makeText(this, "Password Updated!", Toast.LENGTH_SHORT).show()
                finish() // Close screen
            } else {
                Toast.makeText(this, "Enter a valid password", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}