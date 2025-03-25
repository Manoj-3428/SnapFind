package com.example.lostfound.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.app.ActivityCompat
import com.example.lostfound.R
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.example.lostfound.ui.theme.black
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.ui.theme.secondary
import com.example.lostfound.ui.theme.secondary_light
import com.example.lostfound.ui.theme.white
import com.example.lostfound.viewmodel.ComplaintViewModel
import com.example.lostfound.viewmodel.firebase.saveDataToFirebase
import com.example.lostfound.viewmodel.getAddressFromLocation

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission", "UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun addComplaint(navController: NavController) {
    val types = listOf("Wallet", "Bag", "Phone", "Jewelry", "Keys", "Documents", "Others")
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val expanded = remember { mutableStateOf(false) }
    val selectedOption = remember { mutableStateOf(types[0]) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationPermission =
        rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    val address = remember { mutableStateOf("") }
    val latitude = remember { mutableStateOf<Double?>(null) }
    val longitude = remember { mutableStateOf<Double?>(null) }
    val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val description = remember { mutableStateOf("") }
    val contact = remember { mutableStateOf("") }
    val marks = remember { mutableStateOf("") }
    val rewards = remember { mutableStateOf("") }
    val profileUri=remember { mutableStateOf("") }
    val isGetting = remember { mutableStateOf(false) }
    var locationDetails = remember { mutableStateOf<LocationDetails?>(null) }
    var detectionResult = ""
    val notificationPermission =
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
    val keyboardController = LocalSoftwareKeyboardController.current
    val singleImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            imageUri.value = uri
        }
    Scaffold(modifier = Modifier.fillMaxSize()) {it->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(it),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(250.dp)
                    .padding(start = 10.dp, end = 10.dp, top = 20.dp).border(2.dp, black)
                    .clickable {
                        singleImageLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri.value != null) {
                    AsyncImage(
                        model = imageUri.value,
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize().height(250.dp).fillMaxWidth().padding(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.cam),
                        contentDescription = "Placeholder Image",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Fill complaint details",
                color = secondary,
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 10.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ExposedDropdownMenuBox(
                expanded = expanded.value,
                onExpandedChange = { expanded.value = it },
            ) {
                OutlinedTextField(
                    value = selectedOption.value,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(text = "Select Item Type") },

                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "arrow"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp)
                        .menuAnchor()
                        .clickable { expanded.value = !expanded.value },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedLabelColor = secondary_light,
                        unfocusedLabelColor = secondary,
                        focusedBorderColor = secondary_light,
                        unfocusedBorderColor = secondary,
                        focusedTrailingIconColor = secondary_light,
                        unfocusedTrailingIconColor = secondary
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
            OutlinedTextField(
                value = address.value,
                onValueChange = { address.value = it },

                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, top = 10.dp),
                label = {
                    Text(
                        text = if (isGetting.value) "Fetching location..."
                        else "Last seen location",
                        color = if (isGetting.value) Color.Gray else secondary
                    )
                },
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
//                                                    Log.d("Location dtails:",locationDetails.value.toString())
                                                    isGetting.value = false
                                                    address.value=getAddressFromLocation(latitude.value ?: 0.0,
                                                        longitude.value ?: 0.0,
                                                        context)
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = secondary,
                    focusedTrailingIconColor = secondary_light,
                    unfocusedTrailingIconColor = secondary
                )
            )
            OutlinedTextField(
                value = contact.value,
                onValueChange = { contact.value = it },
                label = { Text("Contact Details") },
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = secondary,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = secondary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
            OutlinedTextField(
                value = rewards.value,
                onValueChange = { rewards.value = it },
                label = { Text("Rewards") },
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = secondary,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = secondary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
            OutlinedTextField(
                value = marks.value,
                onValueChange = { marks.value = it },
                label = { Text("Additional Identifiable Marks") },
                modifier = Modifier.fillMaxWidth().height(150.dp).padding(10.dp),
                minLines = 3,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = secondary,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = secondary
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            val isLoading = remember { mutableStateOf(false) }
            val db=FirebaseFirestore.getInstance()
            val userid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            db.collection("users").document(userid).get().addOnSuccessListener { document ->
                val profile = document.toObject(Profiles::class.java)
                if (profile != null) {
                    profileUri.value = profile.uri
                }
            }
            if (isGetting.value == false) {
                Button(
                    onClick = {
                        Log.d("Location dtails:",locationDetails.value.toString())
                        coroutineScope.launch {
                            imageUri.value?.let {
                                isLoading.value = true
                                notificationPermission.launchPermissionRequest()
                                saveDataToFirebase(
                                    detectionResult,
                                    address.value,
                                    notificationPermission.status.isGranted,
                                    it,
                                    context,
                                    selectedOption.value,
                                    description.value,
                                    address.value,
                                    latitude.value.toString(),
                                    longitude.value.toString(),
                                    contact.value,
                                    rewards.value,
                                    profileUri.value,
                                    locationDetails.value!!
                                ) {
                                    isLoading.value = false
                                    navController.navigate("Home")

                                }


                            } ?: Toast.makeText(
                                context,
                                "Please select an image",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    },
                    enabled = !isLoading.value,
                    modifier = Modifier.padding(
                        top = 20.dp,
                        end = 20.dp,
                        start = 20.dp,
                        bottom = 15.dp
                    )
                        .fillMaxWidth().align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary_light,
                        contentColor = white
                    )
                ) {
                    Text(text = "Post")
                }
                if (isLoading.value) {
                    navController.navigate("LottieAnimation")
                }
            }
        }
    }
}
fun getRealPathFromUri(context: Context, uri: Uri): String {
    val file = File(context.cacheDir, "temp_image.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return file.absolutePath
}

