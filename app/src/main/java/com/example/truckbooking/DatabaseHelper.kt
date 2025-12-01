package com.example.truckbooking

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TruckApp.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase?) {
        // 1. Create the Tables
        db?.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT, role TEXT, phone TEXT)")
        db?.execSQL("CREATE TABLE bookings(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, equipment TEXT, date TEXT, price INTEGER, status TEXT, phone TEXT, address TEXT)")

        // 2. AUTO-CREATE THE ADMIN ACCOUNT
        val cv = ContentValues()
        cv.put("username", "admin")
        cv.put("password", "admin123")
        cv.put("role", "Admin")
        cv.put("phone", "1234567890")

        db?.insert("users", null, cv)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS users")
        db?.execSQL("DROP TABLE IF EXISTS bookings")
        onCreate(db)
    }

    // --- USER FUNCTIONS ---
    fun insertUser(username: String, pass: String, role: String, phone: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("password", pass)
        values.put("role", role)
        values.put("phone", phone)
        val result = db.insert("users", null, values)
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
        return db.update("users", values, "username=?", arrayOf(username)) > 0
    }

    // --- NEW ADMIN FUNCTIONS ---
    fun getAllUsers(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT username, role, phone FROM users", null)
        if (cursor.moveToFirst()) {
            do {
                // Format: "John (User) | Phone: 09123..."
                val u = cursor.getString(0)
                val r = cursor.getString(1)
                val p = cursor.getString(2)
                list.add("$u ($r)\nPhone: $p")
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteUser(username: String): Boolean {
        val db = this.writableDatabase
        return db.delete("users", "username=?", arrayOf(username)) > 0
    }

    // --- BOOKING FUNCTIONS ---
    fun isTruckAvailable(equipment: String, date: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings WHERE equipment=? AND date=? AND status != 'Rejected'", arrayOf(equipment, date))
        val count = cursor.count
        cursor.close()
        return count == 0
    }

    fun addBooking(username: String, equipment: String, date: String, price: Int, phone: String, address: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("equipment", equipment)
        values.put("date", date)
        values.put("price", price)
        values.put("status", "Pending")
        values.put("phone", phone)
        values.put("address", address)
        val result = db.insert("bookings", null, values)
        return result != -1L
    }

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
        if (cursor.moveToFirst()) { total = cursor.getInt(0) }
        cursor.close()
        return total
    }

    fun getAllBookingsFormatted(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM bookings", null)
        if (cursor.moveToFirst()) {
            do {
                val s = "${cursor.getString(2)}|${cursor.getString(3)}|${cursor.getInt(4)}|${cursor.getString(5)}|${cursor.getString(1)}|${cursor.getString(6)}|${cursor.getString(7)}"
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
                val s = "${cursor.getString(2)}|${cursor.getString(3)}|${cursor.getInt(4)}|${cursor.getString(5)}|${cursor.getString(1)}|${cursor.getString(6)}|${cursor.getString(7)}"
                list.add(s)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}