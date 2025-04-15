package com.example.lostfound.viewmodel.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

fun deleteMessage(chatId: String, messageId: String) {
    val db = FirebaseFirestore.getInstance()

    db.collection("messages")
        .document(chatId)
        .collection("messages")
        .whereEqualTo("id", messageId)
        .get()
        .addOnSuccessListener { documents ->
            for (doc in documents) {
                db.collection("messages")
                    .document(chatId)
                    .collection("messages")
                    .document(doc.id)
                    .delete().addOnSuccessListener{
                        db.collection("chats")
                            .document(chatId)
                            .update("lastMessage", "This message was deleted", "timestamp", System.currentTimeMillis())
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("DeleteMessage", "Error updating message", e)
        }
}

