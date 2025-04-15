package com.example.lostfound.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.R
import com.example.lostfound.model.Complaint
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.viewmodel.firebase.DeleteMyComplaint
import com.example.lostfound.viewmodel.firebase.DeleteMyImage
import com.example.lostfound.viewmodel.firebase.createChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(complaint: Complaint?, navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            ) {
                if (complaint != null) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Complaint Details",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            actionIconContentColor = Color.Black
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            AsyncImage(
                                model = complaint.imageUri,
                                contentDescription = "Complaint Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Lost Item" ,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = primary_dark,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Divider(color = Color.LightGray, thickness = 1.dp)

                                Spacer(modifier = Modifier.height(8.dp))

                                InfoRow("Date", "${complaint.dayOfWeek}, ${complaint.formattedDate}")
                                InfoRow("Time", "at ${complaint.formattedTime}")
                                InfoRow("Location", complaint.location)
                                InfoRow("Identifiable Marks", complaint.identifiableMarks)
                                InfoRow("Contact", complaint.email)

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Red.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Type",
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Type: ${complaint.type}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Red
                                    )
                                }

                                if (complaint.rewards.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Color(0xFFE8F5E9),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.reward),
                                            contentDescription = "Reward",
                                            tint = Color(0xFF66BB6A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reward: ${complaint.rewards}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uid == complaint.userId) {
                        DeleteButtonSection(complaint, navController, coroutineScope, context)
                    } else {
                        ContactOwnerButton(uid, complaint, navController, db, context)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No complaint details available", color = Color.Gray)
                    }
                }
            }
}

@Composable
private fun DeleteButtonSection(
    complaint: Complaint,
    navController: NavController,
    coroutineScope: CoroutineScope,
    context: android.content.Context
) {
    val isDelete = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red.copy(alpha = 0.9f),
            contentColor = Color.White
        ),
        onClick = { showDialog.value = true },
        enabled = !isDelete.value
    ) {
        if (isDelete.value) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp))
        } else {
            Text("Delete Complaint", fontWeight = FontWeight.SemiBold)
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Confirm Deletion") },
            text = { Text("This will permanently delete your complaint. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                        isDelete.value = true
                        coroutineScope.launch {
                            DeleteMyComplaint(navController, complaint.postId.toString(),
                                onSuccess = {
                                    DeleteMyImage(complaint.imageUri,
                                        onSuccess = {
                                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                            navController.navigate("Home") { popUpTo(0) }
                                        },
                                        onFailure = { isDelete.value = false }
                                    )
                                },
                                onFailure = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    isDelete.value = false
                                }
                            )
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactOwnerButton(
    uid: String?,
    complaint: Complaint,
    navController: NavController,
    db: FirebaseFirestore,
    context: android.content.Context
) {
    val showContactDialog = remember { mutableStateOf(false) }

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = primary_dark,
            contentColor = Color.White
        ),
        onClick = { showContactDialog.value = true }
    ) {
        Text("I Found This Item", fontWeight = FontWeight.SemiBold)
    }

    if (showContactDialog.value) {
        AlertDialog(
            onDismissRequest = { showContactDialog.value = false },
            title = { Text("Contact Owner?") },
            text = { Text("You can message the owner about this item") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showContactDialog.value = false
                        val chatId = "${uid}_${complaint.userId}"

                        db.collection("chats").document(chatId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    navController.navigate("MessageScreen/$chatId/${complaint.userId}")
                                } else {
                                    createChat(chatId, uid.toString(), complaint.userId, context) { createdChatId, otherUserId ->
                                        navController.navigate("MessageScreen/$createdChatId/$otherUserId") {
                                            popUpTo("ChatScreen") { inclusive = true }
                                        }
                                    }
                                }
                            }
                    }
                ) {
                    Text("Contact", color = primary_dark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.width(120.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray.copy(alpha = 0.8f),
            modifier = Modifier.weight(1f))
    }
}