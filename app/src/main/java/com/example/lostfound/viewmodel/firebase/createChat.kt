package com.example.lostfound.viewmodel.firebase

import com.android.volley.Request
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.lostfound.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import androidx.compose.runtime.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot

fun createChat(
    chatId: String,
    user1: String,
    user2: String,
    context: android.content.Context,
    onSuccess: (chatId: String, otherUserId: String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val chatRef = db.collection("chats").document(chatId)
    val user1Task = db.collection("users").document(user1).get()
    val user2Task = db.collection("users").document(user2).get()

    Tasks.whenAllSuccess<DocumentSnapshot>(user1Task, user2Task).addOnSuccessListener { documents ->
        val user1Doc = documents[0]
        val user2Doc = documents[1]

        val user1Name = user1Doc.getString("name") ?: "Unknown"
        val user1Profile = user1Doc.getString("uri") ?: ""
        val user2Name = user2Doc.getString("name") ?: "Unknown"
        val user2Profile = user2Doc.getString("uri") ?: ""

        chatRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val chat = Chat(
                    chatId = chatId,
                    user1 = user1,
                    user2 = user2,
                    lastMessage = "",
                    timestamp = System.currentTimeMillis(),
                    username1 = user1Name,
                    userprofile1 = user1Profile,
                    username2 = user2Name,
                    userprofile2 = user2Profile
                )

                chatRef.set(chat).addOnSuccessListener {
                    Log.d("Chat", "Chat created successfully with both users' info")
                    onSuccess(chatId, user2)
                    val receiverId = if (user1 == FirebaseAuth.getInstance().currentUser?.uid) user2 else user1

                    db.collection("users").document(receiverId).get().addOnSuccessListener { receiverDoc ->
                        val fcmToken = receiverDoc.getString("fcmToken")
                        if (!fcmToken.isNullOrEmpty()) {
                            sendNotification(
                                context,
                                fcmToken,
                                "New Chat Request",
                                "$user1Name wants to contact you."
                            )
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e("Chat", "Error creating chat", e)
                }
            }
        }
    }.addOnFailureListener { e ->
        Log.e("Chat", "Error fetching user data", e)
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
            headers["Authorization"] = "key=YOUR_SERVER_KEY"
            headers["Content-Type"] = "application/json"
            return headers
        }
    }

    Volley.newRequestQueue(context).add(request)
}
