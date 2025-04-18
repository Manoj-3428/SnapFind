package com.example.lostfound.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.R
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.example.lostfound.ui.theme.*
import com.example.lostfound.viewmodel.firebase.profiles
import com.example.lostfound.viewmodel.getAddressFromLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(navController: NavController) {
    // State variables
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val isGettingLocation = remember { mutableStateOf(false) }
    val isUploading = remember { mutableStateOf(false) }
    val locationDetails = remember { mutableStateOf<LocationDetails?>(null) }

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val locationPermission =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUri.value = it } }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                document.toObject(Profiles::class.java)?.let { profile ->
                    name.value = profile.name
                    email.value = profile.email
                    phone.value = profile.phone
                    address.value = profile.address
                    profile.uri?.let { imageUri.value = Uri.parse(it) }
                    locationDetails.value = profile.locationDetails
                }
                isLoading.value = false
            }
        } ?: run { isLoading.value = false }
    }

    if (isLoading.value) {
        LoadingScreen()
    } else {
        Scaffold(modifier = Modifier.fillMaxSize()) { it ->


            Column(
                modifier = Modifier
                    .fillMaxSize().padding(it)
                    .background(Color(0xFFF5F7FA))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(

                        contentAlignment = Alignment.Center
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = if(imageUri.value.toString().isEmpty()) R.drawable.avatar else imageUri.value,
                                contentDescription = "Profile photo",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, primary_light, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(primary_light, CircleShape),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = primary_light,
                                    contentColor = white
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pencil),
                                    contentDescription = "Edit photo",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }


                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .padding(end = 5.dp).align(Alignment.End)
                        .wrapContentSize(),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = white
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.logout),
                            contentDescription = "Logout",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Logout", fontSize = 14.sp)
                    }
                }
                Text(
                    text = "Profile Information",
                    style = MaterialTheme.typography.headlineSmall,
                    color = primary_dark,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(24.dp))

                ProfileTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = "Email Address",
                    icon = Icons.Default.Email,
                    modifier = Modifier.fillMaxWidth()
                )

                ProfileTextField(
                    value = phone.value,
                    onValueChange = { phone.value = it },
                    label = "Phone Number",
                    icon = Icons.Default.Phone,
                    modifier = Modifier.fillMaxWidth()
                )

                LocationField(
                    address = address.value,
                    onValueChange = { address.value = it },
                    isGettingLocation = isGettingLocation.value,
                    onLocationClick = {
                        isGettingLocation.value = true
                        if (locationPermission.status.isGranted) {
                            getCurrentLocation(
                                fusedLocation,
                                onSuccess = { lat, long ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        locationDetails.value =
                                            getAddressFromLocations(lat, long, context)
                                        address.value = getAddressFromLocation(lat, long, context)
                                        isGettingLocation.value = false
                                    }
                                },
                                onFailure = {
                                    address.value = "Location unavailable"
                                    isGettingLocation.value = false
                                }
                            )
                        } else {
                            locationPermission.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if(address.value.isNullOrEmpty()){
                            Toast.makeText(context, "Please enter your address", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        else {
                            isUploading.value = true
                            coroutineScope.launch {
                                profiles(
                                    name = name.value,
                                    email = email.value,
                                    phone = phone.value,
                                    uri = imageUri.value,
                                    address = address.value,
                                    db = db,
                                    storage = storage,
                                    auth = auth,
                                    context = context,
                                    locationDetails = locationDetails.value ?: LocationDetails()
                                ) { isUploading.value = false }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary_light,
                        contentColor = white
                    ),
                    enabled = !isUploading.value && !isGettingLocation.value
                ) {
                    if (isUploading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = white
                        )
                    } else {
                        Text(
                            text = "SAVE PROFILE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = primary_light,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading Profile...",
                color = primary_dark,
                fontSize = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = primary_light
            )
        },
        modifier = modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = primary_light,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primary_light,
            unfocusedLabelColor = Color.Gray,
            cursorColor = primary_light
        ),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationField(
    address: String,
    onValueChange: (String) -> Unit,
    isGettingLocation: Boolean,
    onLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = address,
        onValueChange = onValueChange,
        label = { Text("Current Address") },
        trailingIcon = {
            if (isGettingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = primary_light,
                    strokeWidth = 3.dp
                )
            } else {
                IconButton(
                    onClick = onLocationClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.marker),
                        contentDescription = "Get current location",
                        tint = Color.Unspecified
                    )
                }
            }
        },
        modifier= Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = primary_light,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primary_light,
            unfocusedLabelColor = Color.Gray,
            cursorColor = primary_light
        ),
    )
}

private fun getCurrentLocation(
    fusedLocation: FusedLocationProviderClient,
    onSuccess: (Double, Double) -> Unit,
    onFailure: (Exception) -> Unit
) {
    fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location ->
            location?.let { onSuccess(it.latitude, it.longitude) }
                ?: run { onFailure(Exception("Location not available")) }
        }
        .addOnFailureListener { onFailure(it) }
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
    return if (parts.size > 2) parts[parts.size - 3] else null
}