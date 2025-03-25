package com.example.lostfound.model

import kotlinx.parcelize.Parcelize

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
@Parcelize
data class Complaint(
    val userId: String="",
    val timestamp: Timestamp=Timestamp.now(),
    val username: String="",
    val postId: String="",
    val address: String="",
    val imageUri: String="",
    val type: String="",
    val identifiableMarks: String="",
    val location: String="",
    val latitude: String="",
    val longitude: String="",
    val formattedDate: String="",
    val dayOfWeek: String="",
    val formattedTime: String="",
    val email:String="",
    val contact:String="",
    val rewards:String="",
    val profileUri: String="",
    val locationDetails: LocationDetails = LocationDetails(), // Now Parcelable
    val status:String="Pending",
    val foundOn:String=" ",
): Parcelable{
    constructor() : this("", Timestamp.now(),"", "", "", "", "","","","","","","","","","","","",LocationDetails(),"","")
}