package com.example.talabat
import com.google.firebase.database.FirebaseDatabase
fun syncDriverToFirebase(driver: Driver) {
    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("drivers")

    val driverData = mapOf(
        "name" to driver.name,
        "deliveries" to driver.deliveriesCount
    )

    myRef.push().setValue(driverData)
}