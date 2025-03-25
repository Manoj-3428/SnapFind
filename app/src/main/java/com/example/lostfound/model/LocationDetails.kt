package com.example.lostfound.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocationDetails(
    val latitude: Double,
    val longitude: Double,
    val village: String,
    val district: String?,
    val state: String?,
    val country: String?,

): Parcelable
{
    constructor():this(0.0,0.0,"","","","")
}

