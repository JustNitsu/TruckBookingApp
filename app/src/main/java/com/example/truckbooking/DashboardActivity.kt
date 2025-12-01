package com.example.truckbooking

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import java.util.Calendar
import android.Manifest

class DashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var username: String
    private lateinit var userRole: String
    private lateinit var userPhone: String

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
        username = intent.getStringExtra("NAME") ?: ""
        userRole = intent.getStringExtra("ROLE") ?: ""
        userPhone = db.getUserPhone(username)

        setupHeader()
        setupLogoutButton()

        // === ROLE CHECKER ===
        if (userRole == "User") {
            setupUserMode()
        } else if (userRole == "Owner") {
            setupOwnerMode()
        } else {
            setupAdminMode() // New Admin Mode
        }
    }

    private fun setupHeader() {
        findViewById<TextView>(R.id.txtWelcome).text = "Hello, $username ($userRole)"
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
            startActivity(Intent(this, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            finish()
        }
    }

    // --- ADMIN LOGIC (NEW) ---
    private fun setupAdminMode() {
        findViewById<LinearLayout>(R.id.layoutAdmin).visibility = View.VISIBLE
        val listUsers = findViewById<ListView>(R.id.listAllUsers)

        refreshUserListForAdmin(listUsers)

        listUsers.setOnItemClickListener { _, _, position, _ ->
            val item = listUsers.getItemAtPosition(position).toString()
            val selectedUser = item.split(" ")[0]

            if (selectedUser == username) {
                Toast.makeText(this, "You cannot delete yourself!", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to ban $selectedUser?")
                    .setPositiveButton("Delete") { _, _ ->
                        db.deleteUser(selectedUser)
                        refreshUserListForAdmin(listUsers)
                        Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun refreshUserListForAdmin(listView: ListView) {
        val users = db.getAllUsers()
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
    }

    // --- USER LOGIC ---
    private fun setupUserMode() {
        findViewById<LinearLayout>(R.id.layoutUser).visibility = View.VISIBLE
        val spinnerTruck = findViewById<Spinner>(R.id.spinnerTruck)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etAddress = findViewById<EditText>(R.id.etAddress)
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
            val address = etAddress.text.toString()
            val price = equipmentPrices[truck] ?: 0

            if (date.isNotEmpty() && address.isNotEmpty()) {
                if (db.isTruckAvailable(truck, date)) {
                    if (db.addBooking(username, truck, date, price, userPhone, address)) {
                        Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show()
                        etDate.text.clear()
                        etAddress.text.clear()
                        refreshUserList(listHistory)
                    }
                } else {
                    Toast.makeText(this, "Truck unavailable.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter Date and Address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshUserList(listView: ListView) {
        val list = db.getUserBookings(username)
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

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().lowercase()
                val filteredList = allBookings.filter { it.lowercase().contains(text) }
                val adapter = BookingAdapter(this@DashboardActivity, ArrayList(filteredList)) { equip, date ->
                    showManagementDialog(equip, date, listView)
                }
                listView.adapter = adapter
            }
        })
    }

    private fun refreshOwnerList(listView: ListView) {
        allBookings = db.getAllBookingsFormatted()
        val adapter = BookingAdapter(this, allBookings) { equip, date ->
            showManagementDialog(equip, date, listView)
        }
        listView.adapter = adapter
        findViewById<TextView>(R.id.txtTotalEarnings).text = "$${db.getTotalEarnings()}"
    }

    private fun showManagementDialog(equip: String, date: String, listView: ListView) {
        val options = arrayOf("Approve", "Reject", "Delete", "Print Invoice")
        AlertDialog.Builder(this).setTitle("Manage Order")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        db.updateStatusByDetails(equip, date, "Approved")
                        showNotification("Approved", "Booking for $equip confirmed.")
                    }
                    1 -> db.updateStatusByDetails(equip, date, "Rejected")
                    2 -> db.deleteBooking(equip, date)
                    3 -> printInvoice(equip, date, "Valued Client")
                }
                refreshOwnerList(listView)
            }.show()
    }

    // --- HELPER FUNCTIONS ---
    private fun showNotification(title: String, message: String) {
        val channelId = "booking_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Booking Updates", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        manager.notify(1, builder.build())
    }

    private fun printInvoice(equip: String, date: String, user: String) {
        val price = equipmentPrices[equip] ?: 0
        val htmlDocument = """
            <html><body style="padding: 20px; font-family: Helvetica;">
                <h1 style="color: #1976D2;">TruckBooking Invoice</h1><hr>
                <p><b>Client:</b> $user</p><p><b>Date:</b> $date</p><p><b>Equipment:</b> $equip</p>
                <h2 style="color: #388E3C;">Total: $$price.00</h2><hr>
            </body></html>
        """.trimIndent()
        val webView = WebView(this)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
                val adapter = webView.createPrintDocumentAdapter("Invoice_$date")
                printManager.print("TruckBooking Invoice", adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "UTF-8", null)
    }
}