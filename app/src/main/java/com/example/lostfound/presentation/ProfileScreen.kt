package com.example.lostfound.presentation

import android.content.Context
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.R
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.example.lostfound.ui.theme.black
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.ui.theme.secondary_light
import com.example.lostfound.ui.theme.white
import com.example.lostfound.viewmodel.firebase.profiles
import com.example.lostfound.viewmodel.getAddressFromLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun profile(navController: NavController) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val address = remember { mutableStateOf<String>("") }
    val role = remember { mutableStateOf("") }
    val imageuri = remember { mutableStateOf<Uri?>(null) }
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLoading=remember { mutableStateOf(true) }
    val isGetting = remember { mutableStateOf(false) }
    val latitude = remember { mutableStateOf<Double?>(null) }
    val longitude = remember { mutableStateOf<Double?>(null) }
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val locationPermission =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var locationDetails = remember { mutableStateOf<LocationDetails?>(null) }
    LaunchedEffect(Unit) {
        delay(1000)
        isLoading.value=false
    }
    val singleImagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageuri.value = uri
        }
    }

    val userId = auth.currentUser?.uid
    if (userId != null) {
        LaunchedEffect(userId) {
            loadUserData(db, userId, name, email, phone, address, imageuri)
            Log.d("User uid :",userId)
        }
    }
    if (isLoading.value == true) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primary_dark)
        }
    }
    else {
        Column(modifier = Modifier.fillMaxSize().background(white).verticalScroll(scrollState).imePadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().height(250.dp).padding(), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = imageuri.value ?: R.drawable.avatar,
                    contentDescription = "Profile photo",
                    modifier = Modifier.clip(CircleShape).border(4.dp, primary_dark, CircleShape).
                    clip(CircleShape).size(150.dp), contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { singleImagePicker.launch("image/*") },
                    modifier = Modifier.padding(top = 120.dp, start = 105.dp).clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = white, containerColor = primary_light)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.pencil),
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp),
                        tint = primary_dark
                    )
                }
            }
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                label = { Text(text = "Name", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.secondary_light),
                    unfocusedBorderColor = colorResource(R.color.black),
                    focusedLabelColor = colorResource(R.color.secondary_light),
                    unfocusedLabelColor = colorResource(R.color.black),
                    focusedLeadingIconColor = colorResource(R.color.secondary_light),
                    unfocusedLeadingIconColor = colorResource(R.color.black)
                )
            )
            OutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = { Text(text = "Email", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.mail),
                        contentDescription = "Email",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.secondary_light),
                    unfocusedBorderColor = colorResource(R.color.black),
                    focusedLabelColor = colorResource(R.color.secondary_light),
                    unfocusedLabelColor = colorResource(R.color.black),
                    focusedLeadingIconColor = colorResource(R.color.secondary_light),
                    unfocusedLeadingIconColor = colorResource(R.color.black)
                )
            )
            OutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it },
                label = { Text(text = "Phone number", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.secondary_light),
                    unfocusedBorderColor = colorResource(R.color.black),
                    focusedLabelColor = colorResource(R.color.secondary_light),
                    unfocusedLabelColor = colorResource(R.color.black),
                    focusedLeadingIconColor = colorResource(R.color.secondary_light),
                    unfocusedLeadingIconColor = colorResource(R.color.black)
                )
            )
            OutlinedTextField(
                value = address.value,
                onValueChange = { address.value = it },
                label = { Text(text = "Current address", color = Color.Gray) },
                trailingIcon = {
                    if (isGetting.value) {
                        CircularProgressIndicator(
                            color = primary_dark,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "location",
                            tint = black,
                            modifier = Modifier.clickable {
                                isGetting.value = true
                                if (locationPermission.status.isGranted) {
                                    fusedLocation.getCurrentLocation(
                                        Priority.PRIORITY_HIGH_ACCURACY,
                                        null
                                    )
                                        .addOnSuccessListener { loc ->
                                            if (loc != null) {
                                                latitude.value = loc.latitude
                                                longitude.value = loc.longitude
                                                coroutineScope.launch(Dispatchers.IO) {
                                                     locationDetails.value= getAddressFromLocations(
                                                        latitude.value ?: 0.0,
                                                        longitude.value ?: 0.0,
                                                        context
                                                    )
                                                    isGetting.value = false
                                                    address.value=getAddressFromLocation(latitude.value ?: 0.0,
                                                        longitude.value ?: 0.0,
                                                        context)
                                                    Log.d("Village",locationDetails.value?.village.toString())
                                                    Log.d("District",locationDetails.value?.district.toString())
                                                    Log.d("State",locationDetails.value?.state.toString())
                                                }
                                            } else {
                                                address.value = "Address not found"
                                                isGetting.value = false
                                            }
                                        }.addOnFailureListener {
                                            address.value = it.message.toString()
                                            isGetting.value = false

                                        }
                                } else {
                                    locationPermission.launchPermissionRequest()
                                }
                            })
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = colorResource(R.color.secondary_light),
                    unfocusedBorderColor = colorResource(R.color.black),
                    focusedLabelColor = colorResource(R.color.secondary_light),
                    unfocusedLabelColor = colorResource(R.color.black),
                    focusedLeadingIconColor = colorResource(R.color.secondary_light),
                    unfocusedLeadingIconColor = colorResource(R.color.black)
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            val isUploading = remember { mutableStateOf(false) }
            val currentLocationDetails=locationDetails.value
            Button(
                onClick = {
                    isUploading.value = true
                    coroutineScope.launch {
                        profiles(
                            name.value,
                            email.value,
                            phone.value,
                            imageuri.value,
                            address.value,
                            db,
                            storage,
                            auth,
                            context,
                            if(locationDetails.value!=null){
                                locationDetails.value!!
                            }else{
                                LocationDetails()
                            }
                        ) {
                            isUploading.value = false
                        }
                    }
                },
                modifier = Modifier.wrapContentSize().padding(bottom=20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = secondary_light),
                shape = RoundedCornerShape(4.dp),
                enabled = !isUploading.value
            ) {
                if (isUploading.value) {
                    CircularProgressIndicator(modifier = Modifier.size(40.dp), color = primary_dark)
                } else {
                    Text(text = "Save the data")
                }
            }

        }
    }
}
suspend fun loadUserData(
    db: FirebaseFirestore,
    userId: String,
    name: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    address: MutableState<String>,
    imageUri: MutableState<Uri?>
) {
    db.collection("users").document(userId).get().addOnSuccessListener { document ->
        val user = document.toObject(Profiles::class.java)
        if (user != null) {
            name.value = user.name
            email.value = user.email
            phone.value = user.phone
            address.value = user.address
            imageUri.value = Uri.parse(user.uri)
        }
    }
}
fun getAddressFromLocations(lat: Double, long: Double,context: Context): LocationDetails {
    val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$long&key=AIzaSyARI55ShS61bvK81pmne8_3nPN1CMxN5pQ"
    val response = URL(url).readText()
    val jsonObject = JSONObject(response)
    if (jsonObject.getString("status") == "OK") {
        val results = jsonObject.getJSONArray("results")
        if (results.length() > 0) {
            val addressComponents = results.getJSONObject(0).getJSONArray("address_components")
            var village = ""
            var mandal = ""
            var district = ""
            var state = ""
            var country = ""
            var region=""

            for (i in 0 until addressComponents.length()) {
                val component = addressComponents.getJSONObject(i)
                val types = component.getJSONArray("types")
                when {
                    types.toString().contains("locality") -> village = component.getString("long_name")
                    types.toString().contains("administrative_area_level_4") -> region = component.getString("long_name")
                    types.toString().contains("administrative_area_level_3") -> mandal = component.getString("long_name")
                    types.toString().contains("administrative_area_level_2") -> district = component.getString("long_name")
                    types.toString().contains("administrative_area_level_1") -> state = component.getString("long_name")
                    types.toString().contains("country") -> country = component.getString("long_name")
                }
            }

            return LocationDetails(
                latitude = lat,
                longitude = long,
                village = village,
                district = mandal,
                state = state,
                country = country
            )
        }
    }

    return LocationDetails(
        latitude = lat,
        longitude = long,
        village = "Address not found",
        district = "Not Available",
        state = "Not Available",
        country = "Not Available"
    )
}
fun extractDistrict(addressLine: String?): String? {
    if (addressLine.isNullOrEmpty()) return null

    val parts = addressLine.split(", ")
    return if (parts.size > 2) parts[parts.size - 3] else null  // Extract the 3rd last part as the district
}

