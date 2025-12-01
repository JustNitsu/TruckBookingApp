package com.example.truckbooking

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color

// Updated Constructor: Now accepts a function 'onItemClick'
class BookingAdapter(
    var context: Context,
    var list: ArrayList<String>,
    val onItemClick: (String, String) -> Unit // Logic passed from Dashboard
) : BaseAdapter() {

    override fun getCount(): Int { return list.size }
    override fun getItem(position: Int): Any { return list[position] }
    override fun getItemId(position: Int): Long { return position.toLong() }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false)

        val data = list[position].split("|")
        val equip = data[0]
        val date = data[1]
        val price = data[2]
        val status = data[3]
        val user = data[4]
        val phone = data[5]

        val imgTruck = view.findViewById<ImageView>(R.id.imgTruck)
        val tvEquip = view.findViewById<TextView>(R.id.tvEquipment)
        val tvDetails = view.findViewById<TextView>(R.id.tvDetails)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val tvUser = view.findViewById<TextView>(R.id.tvUser)
        val btnCall = view.findViewById<ImageButton>(R.id.btnCall)

        tvEquip.text = equip
        tvDetails.text = "$date | $$price"
        tvStatus.text = status
        tvUser.text = "By: $user"

        if(status == "Approved") tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        else if(status == "Rejected") tvStatus.setTextColor(Color.RED)
        else tvStatus.setTextColor(Color.parseColor("#FF9800"))

        when(equip) {
            "Excavator" -> imgTruck.setImageResource(android.R.drawable.ic_menu_preferences)
            "Dump Truck" -> imgTruck.setImageResource(android.R.drawable.ic_menu_send)
            "Bulldozer" -> imgTruck.setImageResource(android.R.drawable.ic_menu_mapmode)
            else -> imgTruck.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // --- THE FIX: HANDLE ROW CLICK HERE ---
        // This makes the whole row clickable, ignoring the "stealing" issue
        view.setOnClickListener {
            onItemClick(equip, date) // Run the Approve/Reject logic
        }

        // Handle Call Button separately
        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phone")
            context.startActivity(intent)
        }
        // Force the call button to capture its own focus so it doesn't block the row
        btnCall.isFocusable = false
        btnCall.isFocusableInTouchMode = false

        return view
    }
}