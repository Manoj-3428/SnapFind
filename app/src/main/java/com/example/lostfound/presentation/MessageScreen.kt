package com.example.lostfound.presentation
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.lostfound.model.Passing
import com.example.lostfound.viewmodel.firebase.deleteMessage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MessageScreen(passing: Passing?, navController: NavController){
    val messages = remember { mutableStateListOf<Message>() }
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var newMessageText by remember { mutableStateOf("") }
    val othername=remember { mutableStateOf("") }
    val otherProfile=remember { mutableStateOf("") }
    val otherNumber=remember { mutableStateOf("") }
    val context=LocalContext.current
    val callPermission=rememberPermissionState(android.Manifest.permission.CALL_PHONE)

    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    db.collection("users").document(passing?.userid.toString()).get().addOnSuccessListener{
        othername.value=it.getString("name").toString()
        otherProfile.value=it.getString("uri").toString()
        otherNumber.value=it.getString("phone").toString()
    }
    LaunchedEffect(passing?.chatId.toString()) {
        db.collection("messages")
            .document(passing?.chatId.toString())
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
                    Text(text = "${passing?.userName.toString().capitalize()}", modifier = Modifier.padding(start = 10.dp))
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
                        if (!passing?.profileUri.toString().isEmpty() || !passing?.profileUri.toString().isNullOrEmpty()) {
                            AsyncImage(
                                model = otherProfile.value,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.avatar),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if(callPermission.status.isGranted){
                            val intent=Intent(Intent.ACTION_CALL).apply{
                                data=Uri.parse("tel:${otherNumber.value}")
                            }
                            context.startActivity(intent)
                        }else{
                            callPermission.launchPermissionRequest()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = Color.White
                        )
                    }
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
                    MessageItem(
                        message = message,
                        isCurrentUser = message.sender == currentUserId,
                        onDelete = {
                            if (message.sender == currentUserId) {
                                deleteMessage(passing?.userid.toString(), message.id)
                            }
                        }
                    )
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
                                sendMessage(passing?.chatId.toString(), newMessageText)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: Message,
    isCurrentUser: Boolean,
    onDelete: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    var isSelected by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    // Only show dialog if it's the current user's message
    if (showDialog && isCurrentUser) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                isSelected = false
            },
            title = { Text("Delete Message") },
            text = { Text("Do you want to delete this message?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDialog = false
                    isSelected = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    isSelected = false
                }) {
                    Text("No")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { isSelected = false },
                // Only allow long press if it's the current user's message
                onLongClick = {
                    if (isCurrentUser) {
                        isSelected = true
                        showDialog = true
                    }
                }
            )
            .padding(5.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected && isCurrentUser) Color.LightGray
                else if (isCurrentUser) Color(0xFFDCF8C6) else Color(0xFFEDE8DC)
            ),
            border = if (isSelected && isCurrentUser) BorderStroke(1.dp, Color.Gray) else null,
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
        id=chatId.toString()+System.currentTimeMillis().toString(),
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