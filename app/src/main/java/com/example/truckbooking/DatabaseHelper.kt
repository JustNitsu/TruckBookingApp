package com.example.truckbooking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TruckApp.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT, role TEXT)")
        // Updated: Added 'status' column (Default is Pending)
        db?.execSQL("CREATE TABLE bookings(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, equipment TEXT, date TEXT, price INTEGER, status TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        db?.execSQL("DROP TABLE IF EXISTS bookings")
        onCreate(db)
    }

    // --- USER & AUTH ---
    fun insertUser(username: String, pass: String, role: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("username", username)
        contentValues.put("password", pass)
        contentValues.put("role", role)
        val result = db.insert("users", null, contentValues)
        return result != -1L
    }

    fun checkLogin(username: String, pass: String): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE username=? AND password=?", arrayOf(username, pass))
        if (cursor.moveToFirst()) {
            val role = cursor.getString(2)
            cursor.close()
            return role
        }
        cursor.close()
        return null
    }

    // --- BOOKING LOGIC ---

    // 1. Check Availability (Prevent Double Booking)
    fun isTruckAvailable(equipment: String, date: String): Boolean {
        val db = this.readableDatabase
        // We check if there is any booking for this truck on this date that is NOT 'Rejected'
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE equipment=? AND date=? AND status != 'Rejected'", arrayOf(equipment, date))
        val count = cursor.count
        cursor.close()
        return count == 0 // If count is 0, it is available
    }

    // 2. Add Booking with Status
    fun addBooking(username: String, equipment: String, date: String, price: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("equipment", equipment)
        values.put("date", date)
        values.put("price", price)
        values.put("status", "Pending") // Default status
        val result = db.insert("bookings", null, values)
        return result != -1L
    }

    // 3. Update Status (For Owner)
    fun updateStatus(description: String, newStatus: String) {
        val db = this.writableDatabase
        // Again, using description parsing for simplicity. In a real app, use ID.
        val parts = description.split(" - ")
        if(parts.size >= 2) {
            val equip = parts[0]
            val date = parts[1] // Assuming format: Equip - Date - ...
            db.execSQL("UPDATE bookings SET status=? WHERE equipment=? AND date=?", arrayOf(newStatus, equip, date))
        }
    }

    fun deleteBooking(description: String) {
        val db = this.writableDatabase
        val parts = description.split(" - ")
        if(parts.size >= 2) {
            val equip = parts[0]
            val date = parts[1]
            db.delete("bookings", "equipment=? AND date=?", arrayOf(equip, date))
        }
    }

    // --- LIST RETRIEVAL ---

    // Get ALL bookings (For Owner)
    fun getAllBookings(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings", null)
        if (cursor.moveToFirst()) {
            do {
                // Format: Excavator - 2023-12-05 - Pending (by John)
                val equip = cursor.getString(2)
                val date = cursor.getString(3)
                val status = cursor.getString(5) // Col 5 is status
                val user = cursor.getString(1)

                list.add("$equip - $date - [$status] (by $user)")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // Get ONLY MY bookings (For User)
    fun getUserBookings(username: String): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE username=?", arrayOf(username))
        if (cursor.moveToFirst()) {
            do {
                val equip = cursor.getString(2)
                val date = cursor.getString(3)
                val price = cursor.getInt(4)
                val status = cursor.getString(5)

                list.add("$equip ($date)\nStatus: $status | Cost: $$price")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getTotalEarnings(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(price) FROM bookings WHERE status = 'Approved'", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0) // Get the sum
        }
        cursor.close()
        return total
    }

    fun updatePassword(username: String, newPass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("password", newPass)
        // Update the row where username matches
        val result = db.update("users", values, "username=?", arrayOf(username))
        return result > 0
    }
}