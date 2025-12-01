# TruckBooking App 🚛

This is a native Android project I built using **Kotlin** and **XML**. It's a booking system designed to connect people who need heavy equipment (like excavators or cranes) with the owners of that equipment.

The main idea was to create a system that runs completely offline using a local **SQLite** database, so it doesn't need an internet connection to store bookings or user accounts.

## Project Overview

The app splits users into two roles: **Clients** (Users) and **Owners** (Admins). 

### For Clients (Users):
* **Booking System:** You can pick a truck, tap a date on the calendar, and send a request.
* **Conflict Checking:** I added logic to prevent double-booking. If a truck is already taken on a specific date, the app stops the booking so there are no conflicts.
* **History:** Users can see a list of everything they've booked and check if the owner approved or rejected it.
* **Profile:** Simple settings area to update your password.

### For Owners (Admins):
* **Management Dashboard:** Shows a list of all incoming requests.
* **Approval System:** Owners can long-press or click an item to Approve, Reject, or Delete an order.
* **Earnings Tracker:** There's a counter at the top that automatically sums up the price of all "Approved" jobs so the owner knows how much they earned.
* **Search:** I included a search bar to filter through bookings quickly.

## Technical Details

I kept the tech stack simple and native:
* **Language:** Kotlin
* **Database:** SQLite (Custom `DatabaseHelper` class)
* **Design:** XML Layouts (Linear Layouts & ScrollViews)
* **IDE:** Android Studio

## How to Run This

1.  Clone this repo or download the ZIP file.
2.  Open **Android Studio** and select "Open an Existing Project".
3.  Navigate to the folder you just downloaded.
4.  Let Gradle finish syncing, then hit the **Run** (Play) button.
    * *Note: You can use any Android Emulator or a physical device.*

## Testing the App

Since there's no cloud backend, the database is local to your phone/emulator. Here is the best way to test the full flow:

1.  **Create an Owner Account:** Sign up and choose "Owner" from the dropdown. You'll see the dashboard, but it will be empty.
2.  **Create a User Account:** Log out and sign up again as a "User".
3.  **Make a Booking:** Pick a truck and a date, then submit.
4.  **Approve it:** Log back in as the Owner. You'll see the request. Approve it, and watch the "Total Earnings" update.

---
*Created for educational purposes and portfolio demonstration.*
