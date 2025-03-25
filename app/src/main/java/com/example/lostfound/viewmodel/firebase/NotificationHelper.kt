package com.example.lostfound.viewmodel.firebase

import android.content.Context
import android.util.Log
import com.example.lostfound.model.Complaint
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object FirebaseNotificationHelper {

    private const val FCM_URL = "https://fcm.googleapis.com/v1/projects/781402752354/messages:send"

    suspend fun getAccessToken(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val credentials = GoogleCredentials
                .fromStream(context.assets.open("serviceaccount.json"))
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e("FirebaseNotification", "Error fetching access token", e)
            null
        }
    }
    fun sendNotificationToAll(title: String, body: String, context: Context,complaint: Complaint) {
        CoroutineScope(Dispatchers.IO).launch {
            val token = getAccessToken(context)
            if (token.isNullOrEmpty()) {
                Log.e("FirebaseNotification", "Failed to retrieve access token")
                return@launch
            }

            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("topic", "all")
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                    put("data", JSONObject().apply {
                        put("userId", complaint.userId)
                        put("username", complaint.username)
                        put("postId", complaint.postId)
                        put("address", complaint.location)
                        put("imageUri", complaint.imageUri)
                        put("type", complaint.type)
                    })
                    put("android", JSONObject().apply {
                        put("priority", "high")
                    })
                    put("apns", JSONObject().apply {
                        put("headers", JSONObject().apply {
                            put("apns-priority", "10")
                        })
                    })
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(FCM_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val response = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .build()
                    .newCall(request)
                    .execute()

                Log.d("FirebaseNotification", "Response: ${response.body?.string()}")
            } catch (e: Exception) {
                Log.e("FirebaseNotification", "Error sending notification", e)
            }
        }
    }

}
