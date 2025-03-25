package com.example.lostfound.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

fun login(email:String,password:String,onResult:(String)->Unit){
    val auth= FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
        if(task.isSuccessful){
            subscribeToTopic()
            onResult("Success")
        }else{
            onResult(task.exception?.message.toString())
        }
    }
}
fun subscribeToTopic() {
    FirebaseMessaging.getInstance().subscribeToTopic("all")
        .addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("Subscription failed!")
            }
        }
}
