package com.example.lostfound.model

data class Message(
    val sender: String = "",
    val text: String = "",
    val id:String="",
    val timestamp: Long = System.currentTimeMillis()
)
