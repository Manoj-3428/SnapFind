package com.example.lostfound.viewmodel.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.model.Profiles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

fun profiles(
    name: String = "",
    email: String = "",
    phone: String = "",
    uri: Uri? = null,
    address: String = "",
    db: FirebaseDatabase, // 🔁 Changed type to Realtime DB
    storage: FirebaseStorage,
    auth: FirebaseAuth,
    context: Context,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val user = auth.currentUser ?: run {
        Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
        return
    }

    val userId = user.uid
    val userRef = db.getReference("users").child(userId)

    userRef.get().addOnSuccessListener { snapshot ->
        val existingProfile = snapshot.getValue(Profiles::class.java)
        val existingUri = existingProfile?.uri ?: ""
        val isFirebaseUrl = uri?.toString()?.startsWith("https://firebasestorage.googleapis.com") == true

        when {
            uri != null && !isFirebaseUrl -> {
                uploadImageAndUpdateProfile(
                    uri = uri,
                    userId = userId,
                    storage = storage,
                    name = name,
                    email = email,
                    phone = phone,
                    address = address,
                    db = db,
                    auth = auth,
                    context = context,
                    locationDetails = locationDetails,
                    onComplete = onComplete
                )
            }

            else -> {
                val finalUri = when {
                    isFirebaseUrl -> uri.toString()
                    uri != null -> uri.toString()
                    else -> existingUri
                }

                store(
                    name = name,
                    email = email,
                    phone = phone,
                    uri = finalUri,
                    address = address,
                    db = db,
                    auth = auth,
                    context = context,
                    locationDetails = locationDetails,
                    onComplete = onComplete
                )
            }
        }
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}

private fun uploadImageAndUpdateProfile(
    uri: Uri,
    userId: String,
    storage: FirebaseStorage,
    name: String,
    email: String,
    phone: String,
    address: String,
    db: FirebaseDatabase,
    auth: FirebaseAuth,
    context: Context,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val storageRef = storage.reference.child("profile_images/$userId.jpg")

    storageRef.putFile(uri)
        .addOnSuccessListener {
            storageRef.downloadUrl
                .addOnSuccessListener { downloadUrl ->
                    store(
                        name = name,
                        email = email,
                        phone = phone,
                        uri = downloadUrl.toString(),
                        address = address,
                        db = db,
                        auth = auth,
                        context = context,
                        locationDetails = locationDetails,
                        onComplete = onComplete
                    )
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to get image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context,"Make sure You select a profile image", Toast.LENGTH_SHORT).show()
            Log.e("ProfileUpdate", "Image upload error", e)
            onComplete()
        }
}

fun store(
    name: String,
    email: String,
    phone: String,
    uri: String,
    address: String,
    db: FirebaseDatabase, // 🔁 Realtime DB here too
    auth: FirebaseAuth,
    context: Context,
    locationDetails: LocationDetails,
    onComplete: () -> Unit
) {
    val user = auth.currentUser
    if (user == null) {
        Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
        return
    }

    val uid = user.uid
    val userRef = db.getReference("users").child(uid)

    userRef.get().addOnSuccessListener { snapshot ->
        val existingProfile = snapshot.getValue(Profiles::class.java)
        val finalUri = if (uri.isNotEmpty()) uri else existingProfile?.uri ?: ""

        val profile = Profiles(name, email, phone, finalUri, address, locationDetails)

        userRef.setValue(profile)
            .addOnSuccessListener {
                Toast.makeText(context, "Your Profile updated Successfully", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                onComplete()
            }
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Failed to fetch existing profile: ${e.message}", Toast.LENGTH_SHORT).show()
        onComplete()
    }
}
