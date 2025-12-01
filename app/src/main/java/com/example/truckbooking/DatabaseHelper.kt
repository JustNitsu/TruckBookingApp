package com.example.truckbooking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TruckApp.db", null, 2) { // Version bumped to 2

    override fun onCreate(db: SQLiteDatabase?) {
        // Updated: Users now have a 'phone' column
        db?.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT, role TEXT, phone TEXT)")
        // Updated: Bookings now have a 'phone' column (so owner knows who to call)
        db?.execSQL("CREATE TABLE bookings(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, equipment TEXT, date TEXT, price INTEGER, status TEXT, phone TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        db?.execSQL("DROP TABLE IF EXISTS bookings")
        onCreate(db)
    }

    // --- USER FUNCTIONS ---
    fun insertUser(username: String, pass: String, role: String, phone: String): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("username", username)
        contentValues.put("password", pass)
        contentValues.put("role", role)
        contentValues.put("phone", phone) // Save phone
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

    fun getUserPhone(username: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT phone FROM users WHERE username=?", arrayOf(username))
        if (cursor.moveToFirst()) {
            val phone = cursor.getString(0)
            cursor.close()
            return phone
        }
        cursor.close()
        return ""
    }

    fun updatePassword(username: String, newPass: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("password", newPass)
        val result = db.update("users", values, "username=?", arrayOf(username))
        return result > 0
    }

    // --- BOOKING FUNCTIONS ---
    fun isTruckAvailable(equipment: String, date: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE equipment=? AND date=? AND status != 'Rejected'", arrayOf(equipment, date))
        val count = cursor.count
        cursor.close()
        return count == 0
    }

    fun addBooking(username: String, equipment: String, date: String, price: Int, phone: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("equipment", equipment)
        values.put("date", date)
        values.put("price", price)
        values.put("status", "Pending")
        values.put("phone", phone) // Save phone with booking
        val result = db.insert("bookings", null, values)
        return result != -1L
    }

    fun updateStatus(description: String, newStatus: String) {
        // Parsing logic remains the same
        val parts = description.split(" | ")
        if(parts.size > 0) {
            val idPart = parts[0] // We will store ID hidden in string now for safety
            // For simplicity in this tutorial, we stick to updating by equipment/date matching
            // But let's rely on the Dashboard logic to pass the right data.
            // This is a simplified "Mock" update for the tutorial.
            val db = this.writableDatabase
            // We just update the most recent matching booking for simplicity
            // In a real app, use the ID column.
        }
    }

    // Better Update Status using precise matching
    fun updateStatusByDetails(equip: String, date: String, status: String) {
        val db = this.writableDatabase
        db.execSQL("UPDATE bookings SET status=? WHERE equipment=? AND date=?", arrayOf(status, equip, date))
    }

    fun deleteBooking(equip: String, date: String) {
        val db = this.writableDatabase
        db.delete("bookings", "equipment=? AND date=?", arrayOf(equip, date))
    }

    fun getTotalEarnings(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(price) FROM bookings WHERE status = 'Approved'", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    // --- LIST RETRIEVAL ---
    // We return a special delimited string to help the Custom Adapter:
    // "Excavator|2023-12-01|500|Pending|UserJohn|0912345678"
    fun getAllBookingsFormatted(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings", null)
        if (cursor.moveToFirst()) {
            do {
                // 2=Equip, 3=Date, 4=Price, 5=Status, 1=User, 6=Phone
                val s = "${cursor.getString(2)}|${cursor.getString(3)}|${cursor.getInt(4)}|${cursor.getString(5)}|${cursor.getString(1)}|${cursor.getString(6)}"
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUserBookings(username: String): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE username=?", arrayOf(username))
        if (cursor.moveToFirst()) {
            do {
                val s = "${cursor.getString(2)}|${cursor.getString(3)}|${cursor.getInt(4)}|${cursor.getString(5)}|${cursor.getString(1)}|${cursor.getString(6)}"
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}