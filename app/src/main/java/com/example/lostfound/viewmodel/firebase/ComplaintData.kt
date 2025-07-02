package com.example.lostfound.viewmodel.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.lostfound.model.Complaint
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

@RequiresApi(Build.VERSION_CODES.O)
fun saveDataToFirebase(
    detectionResult: String,
    address: String,
    isGranted: Boolean,
    uri: Uri,
    context: Context,
    type: String,
    description: String,
    location: String,
    latitude: String,
    longitude: String,
    rewards: String,
    profileUri: String,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val formattedDate = currentDate.format(formatter)
    val dayOfWeek = currentDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
    val currentTime = LocalTime.now()
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    val formattedTime = currentTime.format(timeFormatter)

    val userid = FirebaseAuth.getInstance().currentUser?.uid
    var username = ""
    var email = ""

    val db = FirebaseFirestore.getInstance()
    val DB = FirebaseDatabase.getInstance()
    val storage = FirebaseStorage.getInstance()
    var profileUri=""
    if (userid == null) {
        Log.e("FirebaseError", "User not authenticated")
        return
    }

    DB.getReference("users").child(userid).get().addOnSuccessListener { snapshot ->
        snapshot.getValue(Profiles::class.java)?.let { rtProfile ->
            profileUri = rtProfile.uri
        }

        db.collection("users").document(userid).get().addOnSuccessListener { document ->
            val profile = document.toObject(Profiles::class.java)
            if (profile != null) {
                username = profile.name
                email = profile.email
            }

            val postId = "${userid}_${System.currentTimeMillis()}"
            val storageReference = storage.reference.child("complaints_images/$postId.jpg")

            Log.d("UploadDebug", "Starting image compression for postId: $postId")

            val compressedImageByteArray = compressImage(uri, context)

            if (compressedImageByteArray != null) {
                val uploadTask = storageReference.putBytes(compressedImageByteArray)

                uploadTask.addOnSuccessListener {
                    Log.d("UploadDebug", "File uploaded successfully")
                    storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                        Log.d("UploadDebug", "Download URL received: ${downloadUrl}")
                        storeComplaints(
                            userid,
                            isGranted,
                            postId,
                            db,
                            address,
                            downloadUrl.toString(),
                            context,
                            type,
                            description,
                            location,
                            latitude,
                            longitude,
                            formattedDate,
                            dayOfWeek,
                            formattedTime,
                            username,
                            email,
                            rewards,
                            profileUri, // using original parameter
                            locationDetails,
                            onComplete
                        )
                    }.addOnFailureListener {
                        Log.e("UploadError", "Failed to get download URL: ${it.message}")
                        onComplete()
                    }
                }.addOnFailureListener {
                    Log.e("UploadError", "File upload failed: ${it.message}")
                    onComplete()
                }
            } else {
                Log.e("CompressionError", "Image compression failed")
                onComplete()
            }
        }
    }
}

fun compressImage(uri: Uri, context: Context): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
        outputStream.toByteArray()
    } catch (e: Exception) {
        Log.e("CompressionError", "Failed to compress image: ${e.message}")
        null
    }
}

fun storeComplaints(
    userid: String,
    isGranted: Boolean,
    postId: String,
    db: FirebaseFirestore,
    address: String,
    imageUri: String,
    context: Context,
    type: String,
    marks: String,
    location: String,
    latitude: String,
    longitude: String,
    formattedDate: String,
    dayOfWeek: String,
    formattedTime: String,
    username: String?,
    email: String?,
    rewards: String,
    profileUri: String,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val timestamp = Timestamp.now()
    val expireAt = Timestamp(Date(System.currentTimeMillis() + 5L * 24 * 60 * 60 * 1000)) // 5 days
    val complaint = Complaint(
        userid,
        timestamp,
        expireAt,
        username.toString(),
        postId,
        address,
        imageUri,
        type,
        marks,
        location,
        latitude,
        longitude,
        formattedDate,
        dayOfWeek,
        formattedTime,
        email.toString(),
        rewards,
        profileUri,
        locationDetails
    )

    db.collection("complaints").document(postId).set(complaint).addOnSuccessListener {
        Toast.makeText(context, "Complaint saved", Toast.LENGTH_SHORT).show()
        FirebaseNotificationHelper.sendNotificationToAll(
            "New Complaint Reported",
             "A new complaint has been posted by $username",
             context,
            complaint
        )
        onComplete()
    }.addOnFailureListener {
        Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
        onComplete()
    }
}
