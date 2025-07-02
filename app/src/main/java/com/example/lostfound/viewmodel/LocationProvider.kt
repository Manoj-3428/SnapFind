package com.example.lostfound.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.collections.isNotEmpty
@SuppressLint("MissingPermission")
@Composable
fun Location(context: Context, coroutineScope: CoroutineScope): MutableState<List<String>> {

    val latitude = remember { mutableStateOf<Double?>(null) }
    val longitude = remember { mutableStateOf<Double?>(null) }
    val address = remember { mutableStateOf<String?>(null) }
    val final = remember { mutableStateOf<List<String>>(emptyList()) }

    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)

    fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { loc ->
            if (loc != null) {
                latitude.value = loc.latitude
                longitude.value = loc.longitude
                coroutineScope.launch(Dispatchers.IO) {
                    address.value = getAddressFromLocation(latitude.value!!, longitude.value!!, context)
                    // Update final list after fetching the address
                    final.value = listOf(
                        latitude.value.toString(),
                        longitude.value.toString(),
                        address.value.toString()
                    )
                }
            } else {
                final.value = listOf("0", "0", "Address not found")
            }
        }
    return final
}

fun getAddressFromLocation(lat: Double, long: Double, context: Context): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    val addresses = geocoder.getFromLocation(lat, long, 1)
    return if (addresses != null && addresses.isNotEmpty()) {
        "${addresses[0].getAddressLine(0)}, ${addresses[0].locality}, ${addresses[0].adminArea}, ${addresses[0].countryName}"
    } else {
        "Address not found"
    }
}


