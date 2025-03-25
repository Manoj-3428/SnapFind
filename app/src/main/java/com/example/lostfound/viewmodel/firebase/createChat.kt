package com.example.lostfound.viewmodel.firebase

import com.android.volley.Request
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lostfound.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject

fun createChat(chatId: String, user1: String, user2: String, context: android.content.Context) {
    val db = FirebaseFirestore.getInstance()
    val chatRef = db.collection("chats").document(chatId)

    chatRef.get().addOnSuccessListener { document ->
        if (!document.exists()) {
            val chat = Chat(
                chatId = chatId,
                user1 = user1,
                user2 = user2,
                lastMessage = "",
                timestamp = System.currentTimeMillis()
            )
            chatRef.set(chat).addOnSuccessListener {
                Log.d("Chat", "Chat created successfully")
                val receiverId = if (user1 == FirebaseAuth.getInstance().currentUser?.uid) user2 else user1
                db.collection("users").document(receiverId).get().addOnSuccessListener { userDoc ->
                    val fcmToken = userDoc.getString("fcmToken")
                    if (!fcmToken.isNullOrEmpty()) {
                        sendNotification(context, fcmToken, "New Chat Request", "Someone wants to contact you.")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("Chat", "Error creating chat", e)
            }
        }
    }
}

fun sendNotification(context: android.content.Context, fcmToken: String, title: String, message: String) {
    val json = JSONObject()
    val notification = JSONObject()
    val data = JSONObject()

    notification.put("title", title)
    notification.put("body", message)

    data.put("click_action", "FLUTTER_NOTIFICATION_CLICK")

    json.put("to", fcmToken)
    json.put("notification", notification)
    json.put("data", data)

    val request = object : JsonObjectRequest(
        Request.Method.POST,
        "https://fcm.googleapis.com/fcm/send",
        json,
        { response -> Log.d("FCM", "Notification sent: $response") },
        { error -> Log.e("FCM", "Error sending notification", error) }
    ) {
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Authorization"] = "key=YOUR_SERVER_KEY" // Replace with your FCM server key
            headers["Content-Type"] = "application/json"
            return headers
        }
    }

    Volley.newRequestQueue(context).add(request)
}
