package com.example.truckbooking

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var username: String
    private lateinit var userRole: String
    private lateinit var userPhone: String

    private var allBookings = ArrayList<String>() // Raw data list

    private val equipmentPrices = mapOf(
        "Excavator" to 500,
        "Dump Truck" to 300,
        "Bulldozer" to 600,
        "Crane" to 800
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        db = DatabaseHelper(this)
        username = intent.getStringExtra("NAME") ?: ""
        userRole = intent.getStringExtra("ROLE") ?: ""

        // Get user phone
        userPhone = db.getUserPhone(username)

        setupHeader()
        setupLogoutButton()

        if (userRole == "User") {
            setupUserMode()
        } else {
            setupOwnerMode()
        }
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.txtWelcome).text = "Hello, $username"
        findViewById<ImageButton>(R.id.btnProfile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("NAME", username)
            startActivity(intent)
        }
    }

    private fun setupLogoutButton() {
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val prefs = getSharedPreferences("TruckApp", MODE_PRIVATE)
            prefs.edit().clear().apply()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // --- USER LOGIC ---
    private fun setupUserMode() {
        findViewById<LinearLayout>(R.id.layoutUser).visibility = View.VISIBLE
        val spinnerTruck = findViewById<Spinner>(R.id.spinnerTruck)
        val etDate = findViewById<EditText>(R.id.etDate)
        val listHistory = findViewById<ListView>(R.id.listUserHistory)

        val truckList = equipmentPrices.keys.toTypedArray()
        spinnerTruck.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, truckList)

        refreshUserList(listHistory)

        etDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d -> etDate.setText("$y-${m+1}-$d") }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btnBook).setOnClickListener {
            val truck = spinnerTruck.selectedItem.toString()
            val date = etDate.text.toString()
            val price = equipmentPrices[truck] ?: 0

            if (date.isNotEmpty()) {
                if (db.isTruckAvailable(truck, date)) {
                    if (db.addBooking(username, truck, date, price, userPhone)) {
                        Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show()
                        etDate.text.clear()
                        refreshUserList(listHistory)
                    }
                } else {
                    Toast.makeText(this, "Truck unavailable.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun refreshUserList(listView: ListView) {
        val list = db.getUserBookings(username)
        // FIX: The adapter now needs 3 arguments.
        // For users, we pass an empty function { _, _ -> } because clicking does nothing for them.
        val adapter = BookingAdapter(this, list) { _, _ -> }
        listView.adapter = adapter
    }

    // --- OWNER LOGIC ---
    private fun setupOwnerMode() {
        findViewById<LinearLayout>(R.id.layoutOwner).visibility = View.VISIBLE
        val listView = findViewById<ListView>(R.id.listView)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        refreshOwnerList(listView)

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            refreshOwnerList(listView)
            etSearch.text.clear()
        }

        // Search Logic
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().lowercase()
                val filteredList = allBookings.filter { it.lowercase().contains(text) }

                // IMPORTANT: Even when searching, we must pass the click logic!
                val adapter = BookingAdapter(this@DashboardActivity, ArrayList(filteredList)) { equip, date ->
                    showManagementDialog(equip, date, listView)
                }
                listView.adapter = adapter
            }
        })
    }

    private fun refreshOwnerList(listView: ListView) {
        allBookings = db.getAllBookingsFormatted()

        // Pass the click logic directly into the Adapter
        val adapter = BookingAdapter(this, allBookings) { equip, date ->
            // This code runs when a row is clicked
            showManagementDialog(equip, date, listView)
        }

        listView.adapter = adapter
        findViewById<TextView>(R.id.txtTotalEarnings).text = "$${db.getTotalEarnings()}"
    }

    private fun showManagementDialog(equip: String, date: String, listView: ListView) {
        val options = arrayOf("Approve", "Reject", "Delete")
        AlertDialog.Builder(this).setTitle("Manage Order")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> db.updateStatusByDetails(equip, date, "Approved")
                    1 -> db.updateStatusByDetails(equip, date, "Rejected")
                    2 -> db.deleteBooking(equip, date)
                }
                refreshOwnerList(listView)
            }.show()
    }
}
