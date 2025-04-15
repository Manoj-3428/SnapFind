package com.example.lostfound.model
data class Chat(
    var chatId: String = "",
    val lastMessage: String = "",
    val timestamp: Long = 0L,
    val user1: String = "",
    val user2: String = "",
    val username1: String = "",
    val username2: String = "",
    val userprofile1: String = "",
    val userprofile2: String = ""
) {
    constructor() : this("", "", 0L, "", "", "", "", "", "")
}


