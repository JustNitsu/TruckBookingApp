package com.example.truckbooking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color

class BookingAdapter(
    var context: Context,
    var list: ArrayList<String>,
    val onItemClick: (String, String) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int { return list.size }
    override fun getItem(position: Int): Any { return list[position] }
    override fun getItemId(position: Int): Long { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false)

        val data = list[position].split("|")
        // Safety check: Ensure we have enough data fields
        if (data.size < 7) return view

        val equip = data[0]
        val date = data[1]
        val price = data[2]
        val status = data[3]
        val user = data[4]
        val phone = data[5]
        val address = data[6] // Get the Address

        val imgTruck = view.findViewById<ImageView>(R.id.imgTruck)
        val tvEquip = view.findViewById<TextView>(R.id.tvEquipment)
        val tvDetails = view.findViewById<TextView>(R.id.tvDetails)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvUser = view.findViewById<TextView>(R.id.tvUser)
        val btnCall = view.findViewById<ImageButton>(R.id.btnCall)
        val btnMap = view.findViewById<ImageButton>(R.id.btnMap) // The new Map Button

        tvEquip.text = equip
        tvDetails.text = "$date | $$price"
        tvStatus.text = status
        tvUser.text = "By: $user\nLoc: $address" // Show address on screen

        // Colors
        if(status == "Approved") tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        else if(status == "Rejected") tvStatus.setTextColor(Color.RED)
        else tvStatus.setTextColor(Color.parseColor("#FF9800"))

        // Images
        when(equip) {
            "Excavator" -> imgTruck.setImageResource(android.R.drawable.ic_menu_preferences)
            "Dump Truck" -> imgTruck.setImageResource(android.R.drawable.ic_menu_send)
            else -> imgTruck.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        view.setOnClickListener { onItemClick(equip, date) }

        // Call Logic
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            context.startActivity(intent)
        }
        btnCall.isFocusable = false

        // MAP LOGIC
        btnMap.setOnClickListener {
            // Open Google Maps searching for the address
            val mapUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // If Google Maps isn't installed, open browser map
                val generalMapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                context.startActivity(generalMapIntent)
            }
        }
        btnMap.isFocusable = false

        return view
    }
}