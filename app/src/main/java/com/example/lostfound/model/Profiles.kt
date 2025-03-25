package com.example.lostfound.model

import com.google.firebase.firestore.PropertyName

data class Profiles(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val uri: String = "",
    val address: String = "",
    val locationDetails: LocationDetails = LocationDetails(),
    val fcmToken: String = "",

) {
    constructor() : this("", "", "", "", "",  LocationDetails(),"")
}