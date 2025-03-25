package com.example.lostfound.presentation
import com.example.lostfound.R
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.model.Chat
import com.example.lostfound.ui.theme.primary
import com.example.lostfound.ui.theme.primary_dark
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController) {
    val chats = remember { mutableStateListOf<Chat>() }
    val db = FirebaseFirestore.getInstance()
    val scrollState= rememberScrollState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var otheridName=remember { mutableStateOf("Unknown User") }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            db.collection("chats")
                .where(
                    Filter.or(
                        Filter.equalTo("user1", currentUserId),
                        Filter.equalTo("user2", currentUserId)
                    )
                )
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ChatScreen", "Error fetching chats", error)
                        return@addSnapshotListener
                    }
                    Log.d("ChatScreen", "Number of chats fetched: ${snapshot?.size()}")
                    chats.clear()
                    snapshot?.documents?.forEach { document ->
                        val chat = document.toObject(Chat::class.java)
                        if (chat != null) {
                            Log.d("ChatScreen", "Chat fetched: $chat")
                            chats.add(chat)
                        }
                    }
                }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary_dark,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding()
        ) {
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No chats found", color = Color.Gray)
                }
            } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                    ) {
                        items(chats) { chat ->
                            ChatItem(chat = chat, onClick = {
                                val otherid = chat.user2
                                navController.navigate("MessageScreen/${chat.chatId}/$otherid")
                            })
                        }
                    }

            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val otherUserId = if (chat.user1 == currentUserId) chat.user2 else chat.user1
    var otherUsername by remember { mutableStateOf("Unknown") }
    var profilePictureUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(otherUserId) {
        if (otherUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener { document ->
                    otherUsername = document.getString("name")?.capitalize() ?: "Unknown User"
                    profilePictureUrl = document.getString("uri")
                }
                .addOnFailureListener {
                    otherUsername = "Unknown User"
                }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top=5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top=5.dp, bottom = 5.dp, start = 10.dp,end=15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(!profilePictureUrl.isNullOrEmpty()){
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            else{
                Image(
                    painter = painterResource(id=R.drawable.avatar),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = otherUsername,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primary
                )
                Text(
                    text = chat.lastMessage,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.timestamp)),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}