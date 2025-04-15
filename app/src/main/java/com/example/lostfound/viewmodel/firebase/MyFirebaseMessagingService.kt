package com.example.lostfound.viewmodel.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lostfound.MainActivity
import com.example.lostfound.R
import com.example.lostfound.model.Complaint
import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.internal.userAgent
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "New Alert"
        val message = remoteMessage.notification?.body ?: "Check the app!"
        Log.d("Remote Message is :","${remoteMessage}")
        val complaint = Complaint(
            userId = remoteMessage.data["userId"] ?: "",
            timestamp = Timestamp.now(),
            username = remoteMessage.data["username"] ?: "",
            postId = remoteMessage.data["postId"] ?: "",
            address = remoteMessage.data["address"] ?: "",
            imageUri = remoteMessage.data["imageUri"] ?: "",
            type = remoteMessage.data["type"] ?: "",
            identifiableMarks = remoteMessage.data["identifiableMarks"] ?: "",
            location = remoteMessage.data["location"] ?: "",
            latitude = remoteMessage.data["latitude"] ?: "",
            longitude = remoteMessage.data["longitude"] ?: "",
            formattedDate = remoteMessage.data["formattedDate"] ?: "",
            dayOfWeek = remoteMessage.data["dayOfWeek"] ?: "",
            formattedTime = remoteMessage.data["formattedTime"] ?: "",
            email = remoteMessage.data["email"] ?: "",
            rewards = remoteMessage.data["rewards"] ?: "",
            profileUri = remoteMessage.data["profileUri"] ?: "",
            status = remoteMessage.data["status"] ?: "Pending",
            foundOn = remoteMessage.data["foundOn"] ?: " "
        )
        Log.w("Complaint is :","${complaint}")
        val imageUrl = complaint.imageUri
        if (imageUrl.isNotEmpty()) {
            loadBitmapFromUrl(imageUrl) { bitmap ->
                showNotification(complaint.username, complaint.type, bitmap, complaint)
            }
        } else {
            showNotification(title, message, null, complaint)
        }
    }

    private fun showNotification(title: String, type: String, image: Bitmap?, complaint: Complaint) {
        val channelId = "default_channel"
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("complaintId", complaint.postId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.mail)
            .setContentTitle("Posted by ${title}")
            .setContentText("${type} is missing")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (image != null) {
            val style = NotificationCompat.BigPictureStyle()
                .bigPicture(image)
                .bigLargeIcon(null as Bitmap?)
            notificationBuilder.setStyle(style)
        }


        NotificationManagerCompat.from(this).notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Default Channel", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    private fun loadBitmapFromUrl(imageUrl: String, callback: (Bitmap?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                callback(bitmap)
            } catch (e: Exception) {
                Log.e("FirebaseNotification", "Error loading image", e)
                callback(null)
            }
        }
    }
}
