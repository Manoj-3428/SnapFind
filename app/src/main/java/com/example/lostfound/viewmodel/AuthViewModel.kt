package com.example.lostfound.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun updateFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

            db.collection("users").document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("TAG", "FCM token updated") }
                .addOnFailureListener { Log.w("TAG", "Failed to update FCM token", it) }
        }
    }
}
