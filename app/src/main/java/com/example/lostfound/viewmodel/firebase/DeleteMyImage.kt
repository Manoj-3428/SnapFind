package com.example.lostfound.viewmodel.firebase
import androidx.navigation.NavController
import com.google.firebase.storage.FirebaseStorage

fun DeleteMyImage(imageUri: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUri)
    storageRef.delete().addOnSuccessListener {
        onSuccess()
    }.addOnFailureListener {
        onFailure(it.message.toString())
    }
}

