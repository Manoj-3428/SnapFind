package com.example.lostfound.presentation

import android.Manifest
import android.R.attr.description
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.R
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.example.lostfound.ui.theme.*
import com.example.lostfound.viewmodel.firebase.saveDataToFirebase
import com.example.lostfound.viewmodel.getAddressFromLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddComplaintScreen(navController: NavController) {
    // State variables
    val types = listOf("Wallet", "Bag", "Phone", "Jewelry", "Keys", "Documents", "Others")
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val expanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf(types[0]) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationPermission = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val notificationPermission = rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    val keyboardController = LocalSoftwareKeyboardController.current

    // Form fields
    val address = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf<Double?>(null) }
    val longitude = remember { mutableStateOf<Double?>(null) }
    val rewards = remember { mutableStateOf("") }
    val marks = remember { mutableStateOf("") }
    val profileUri = remember { mutableStateOf("") }
    val isGettingLocation = remember { mutableStateOf(false) }
    val locationDetails = remember { mutableStateOf<LocationDetails?>(null) }
    val isLoading = remember { mutableStateOf(false) }

    // Services
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri.value = uri }

    // Load user profile
    LaunchedEffect(userId) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            document.toObject(Profiles::class.java)?.let { profile ->
                profileUri.value = profile.uri ?: ""
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Report Lost Item",
                color = primary_dark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Image Upload Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 16.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.value != null) {
                    AsyncImage(
                        model = imageUri.value,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.cam),
                            contentDescription = "Camera Icon",
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Upload Item Photo",
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Item Type Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = selectedOption.value,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Item Type") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown arrow"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = secondary_light,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = secondary_light,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedOption.value = type
                                expanded.value = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Field
            OutlinedTextField(
                value = address.value,
                onValueChange = { address.value = it },
                label = {
                    Text(
                        if (isGettingLocation.value) "Fetching location..."
                        else "Last seen location"
                    )
                },
                trailingIcon = {
                    if (isGettingLocation.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = primary_dark,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Get location",
                            modifier = Modifier.clickable {
                                isGettingLocation.value = true
                                if (locationPermission.status.isGranted) {
                                    fusedLocation.getCurrentLocation(
                                        Priority.PRIORITY_HIGH_ACCURACY,
                                        null
                                    ).addOnSuccessListener { location ->
                                        location?.let { loc ->
                                            latitude.value = loc.latitude
                                            longitude.value = loc.longitude
                                            coroutineScope.launch(Dispatchers.IO) {
                                                locationDetails.value = getAddressFromLocations(
                                                    lat = loc.latitude,
                                                    long = loc.longitude,
                                                    context = context
                                                )
                                                address.value = getAddressFromLocation(
                                                    lat = loc.latitude,
                                                    long = loc.longitude,
                                                    context = context
                                                )
                                                isGettingLocation.value = false
                                            }
                                        } ?: run {
                                            address.value = "Location not available"
                                            isGettingLocation.value = false
                                        }
                                    }.addOnFailureListener {
                                        address.value = it.message ?: "Location error"
                                        isGettingLocation.value = false
                                    }
                                } else {
                                    locationPermission.launchPermissionRequest()
                                }
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = Color.Gray
                ),
                keyboardActions = KeyboardActions(
                    onDone = { keyboardController?.hide() }
                )
            )



            Spacer(modifier = Modifier.height(8.dp))

            // Reward Information
            OutlinedTextField(
                value = rewards.value,
                onValueChange = { rewards.value = it },
                label = { Text("Reward (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Identifiable Marks
            OutlinedTextField(
                value = marks.value,
                onValueChange = { marks.value = it },
                label = { Text("Identifiable Marks") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 16.dp),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.LightGray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (imageUri.value == null) {
                        Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading.value = true
                    notificationPermission.launchPermissionRequest()

                    imageUri.value?.let { uri ->
                        coroutineScope.launch {
                            saveDataToFirebase(
                                detectionResult = "",
                                address = address.value,
                                uri = uri,
                                isGranted = notificationPermission.status.isGranted,
                                context = context,
                                type = selectedOption.value,
                                description = marks.value,
                                location = address.value,
                                latitude = latitude.value?.toString() ?: "",
                                longitude = longitude.value?.toString() ?: "",
                                rewards = rewards.value,
                                profileUri = profileUri.value,
                                locationDetails = locationDetails.value ?: LocationDetails()
                            ) {
                                isLoading.value = false
                                navController.navigate("Home")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary_light,
                    contentColor = white
                ),
                enabled = !isLoading.value
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = white
                    )
                } else {
                    Text(
                        text = "SUBMIT REPORT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

