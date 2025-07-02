package com.example.lostfound.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.lostfound.model.Profiles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

fun createUser(context: Context, name: String, email: String, password: String, onResult: (String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            store(context, name, email)
            subscribeToTopic()
            onResult("Success")
        } else {
            onResult(task.exception?.message.toString())
        }
    }
}

fun store(context: Context, name: String, email: String) {
    val db = FirebaseFirestore.getInstance()
    val rtdb = FirebaseDatabase.getInstance().reference
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid == null) {
        Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            return@addOnCompleteListener
        }

        val fcmToken = task.result
        val user = Profiles(
            name = name,
            email = email,
            phone = "",
            uri = "",
            address = "",
            fcmToken = fcmToken
        )

        // 1. Store in Firestore
        db.collection("users").document(uid).set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User profile saved to Firestore.")
            }
            .addOnFailureListener {
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                Log.w("Firestore", "Error writing document", it)
            }

        // 2. Store in Realtime Database
        rtdb.child("users").child(uid).setValue(user)
            .addOnSuccessListener {
                Log.d("RealtimeDB", "User profile saved to Realtime DB.")
            }
            .addOnFailureListener {
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                Log.w("RealtimeDB", "Error writing to Realtime DB", it)
            }
    }
}
