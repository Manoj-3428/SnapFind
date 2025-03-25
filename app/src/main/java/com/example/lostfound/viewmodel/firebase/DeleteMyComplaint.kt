package com.example.lostfound.viewmodel.firebase

import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

fun DeleteMyComplaint(navController: NavController,postId:String,onSuccess:()->Unit,onFailure:(String)->Unit){
    val db= FirebaseFirestore.getInstance()
    db.collection("complaints").document(postId).delete().addOnSuccessListener({
        onSuccess()
        navController.navigate("Home") {
            popUpTo("addComplaint") { inclusive = true }
        }
    }).addOnFailureListener({
        onFailure(it.message.toString())
    })
}