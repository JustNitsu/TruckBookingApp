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

    // List to hold data for searching
    private var allBookings = ArrayList<String>()

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
        username = intent.getStringExtra("NAME") ?: "User"
        userRole = intent.getStringExtra("ROLE") ?: "User"

        setupHeader()
        setupLogoutButton()

        if (userRole == "User") {
            setupUserMode()
        } else {
            setupOwnerMode()
        }
    }

    private fun setupHeader() {
        val txtWelcome = findViewById<TextView>(R.id.txtWelcome)
        txtWelcome.text = "Hello, $username"


        val btnProfile = findViewById<ImageButton>(R.id.btnProfile)
        btnProfile.setOnClickListener {
            // Check if this Toast appears when you click
            Toast.makeText(this, "Opening Settings...", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("NAME", username)
            startActivity(intent)
        }
    }

    private fun setupLogoutButton() {
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
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
        val btnBook = findViewById<Button>(R.id.btnBook)
        val listHistory = findViewById<ListView>(R.id.listUserHistory)

        val truckList = equipmentPrices.keys.toTypedArray()
        spinnerTruck.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, truckList)

        refreshUserHistory(listHistory)

        etDate.setOnClickListener {
            showDatePicker(etDate)
        }

        btnBook.setOnClickListener {
            val truck = spinnerTruck.selectedItem.toString()
            val date = etDate.text.toString()
            val price = equipmentPrices[truck] ?: 0

            if (date.isNotEmpty()) {
                if (db.isTruckAvailable(truck, date)) {
                    if (db.addBooking(username, truck, date, price)) {
                        Toast.makeText(this, "Booking Sent for Approval!", Toast.LENGTH_LONG).show()
                        etDate.text.clear()
                        refreshUserHistory(listHistory)
                    } else {
                        Toast.makeText(this, "Error saving booking", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Sorry! This truck is already booked on that date.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Select a date first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshUserHistory(listView: ListView) {
        val myBookings = db.getUserBookings(username)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, myBookings)
    }

    private fun showDatePicker(target: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            target.setText("$y-${m + 1}-$d")
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    // --- OWNER LOGIC ---
    private fun setupOwnerMode() {
        findViewById<LinearLayout>(R.id.layoutOwner).visibility = View.VISIBLE
        val listView = findViewById<ListView>(R.id.listView)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        refreshOwnerList(listView)

        // SEARCH LOGIC
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Filter the list
                val text = s.toString().lowercase()
                val filteredList = allBookings.filter { it.lowercase().contains(text) }
                listView.adapter = ArrayAdapter(this@DashboardActivity, android.R.layout.simple_list_item_1, filteredList)
            }
        })

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            refreshOwnerList(listView)
            etSearch.text.clear() // Clear search on refresh
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            // Note: When searching, we must get the item from the adapter, not the global list
            val item = listView.adapter.getItem(position).toString()
            showManagementDialog(item, listView)
        }
    }

    private fun refreshOwnerList(listView: ListView) {
        allBookings = db.getAllBookings() // Update global list
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, allBookings)

        // Update Earnings Text
        val total = db.getTotalEarnings()
        val txtEarnings = findViewById<TextView>(R.id.txtTotalEarnings)
        txtEarnings.text = "$$total"
    }

    private fun showManagementDialog(itemDesc: String, listView: ListView) {
        val options = arrayOf("Approve Order", "Reject Order", "Delete Order")

        AlertDialog.Builder(this)
            .setTitle("Manage Order")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Approve
                        db.updateStatus(itemDesc, "Approved")
                        Toast.makeText(this, "Order Approved", Toast.LENGTH_SHORT).show()
                    }
                    1 -> { // Reject
                        db.updateStatus(itemDesc, "Rejected")
                        Toast.makeText(this, "Order Rejected", Toast.LENGTH_SHORT).show()
                    }
                    2 -> { // Delete
                        db.deleteBooking(itemDesc)
                        Toast.makeText(this, "Order Deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                refreshOwnerList(listView)
            }
            .show()
    }
}