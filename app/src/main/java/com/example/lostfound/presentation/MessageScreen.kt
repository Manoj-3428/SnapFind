package com.example.lostfound.presentation
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.model.Message
import com.example.lostfound.ui.theme.primary
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.white
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import com.example.lostfound.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(otherId:String,chatId: String, navController: NavController) {
    val messages = remember { mutableStateListOf<Message>() }
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var newMessageText by remember { mutableStateOf("") }
    val othername=remember { mutableStateOf("") }
    val otherProfile=remember { mutableStateOf("") }
    db.collection("users").document(otherId).get().addOnSuccessListener{
        othername.value=it.getString("name").toString()
        otherProfile.value=it.getString("uri").toString()
    }
    LaunchedEffect(chatId) {
        db.collection("messages")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MessageScreen", "Error fetching messages", error)
                    return@addSnapshotListener
                }
                messages.clear()
                snapshot?.documents?.forEach { document ->
                    val message = document.toObject(Message::class.java)
                    if (message != null) {
                        messages.add(message)
                    }
                }
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "${othername.value.capitalize()}", modifier = Modifier.padding(start = 10.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary_dark,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { navController.navigate("ChatScreen") }
                    ) {
                        IconButton(
                            onClick = { navController.navigate("ChatScreen") },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                        ) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                        if (!otherProfile.value.isNullOrEmpty()) {
                            AsyncImage(
                                model = otherProfile.value,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    ,
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.avatar),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    ,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                actions = {

                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start=10.dp,end=10.dp).padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageItem(message = message)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    placeholder = {
                        Text(
                            text = "Message",
                            color = Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f).heightIn(min = 30.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(40.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Gray,
                        unfocusedBorderColor = Color.Gray
                    )

                )
                Box(
                    modifier = Modifier.padding(start = 5.dp).wrapContentSize()
                        .background(primary, shape = RoundedCornerShape(50.dp))
                ) {
                    IconButton(
                        onClick = {
                            if (newMessageText.isNotEmpty()) {
                                sendMessage(chatId, newMessageText)
                                newMessageText = ""
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = white
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val isCurrentUser = message.sender == FirebaseAuth.getInstance().currentUser?.uid
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFFEDE8DC)
            ),
            modifier = Modifier
                .widthIn(max = screenWidth * 0.7f)
                .padding(start = if (!isCurrentUser) 5.dp else 0.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = Color.Black
            )
        }
    }
}

fun sendMessage(chatId: String, text: String) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    val message = Message(
        sender = currentUserId!!,
        text = text,
        timestamp = System.currentTimeMillis()
    )
    db.collection("messages")
        .document(chatId)
        .collection("messages")
        .add(message)
        .addOnSuccessListener {
            db.collection("chats")
                .document(chatId)
                .update("lastMessage", text, "timestamp", System.currentTimeMillis())
        }
        .addOnFailureListener { e ->
            Log.e("MessageScreen", "Error sending message", e)
        }
}