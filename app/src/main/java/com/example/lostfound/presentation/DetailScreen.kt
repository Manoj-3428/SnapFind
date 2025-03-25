package com.example.lostfound.presentation
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.lostfound.model.Complaint
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.ui.theme.secondary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.example.lostfound.R
import com.example.lostfound.ui.theme.white
import com.example.lostfound.viewmodel.firebase.DeleteMyComplaint
import com.example.lostfound.viewmodel.firebase.DeleteMyImage
import com.example.lostfound.viewmodel.firebase.createChat
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.toString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(complaint: Complaint?, navController: NavController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var db= FirebaseFirestore.getInstance()
    complaint?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.LightGray, Color.White)
                    )
                )
                .verticalScroll(scrollState)
                .padding(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Complaint Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
//                AsyncImage(
//                    model = complaint.profileUri.ifEmpty { R.drawable.avatar },
//                    contentDescription = "User Profile Image",
//                    modifier = Modifier
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(Color.White),
//                    contentScale = ContentScale.Crop
//                )
//                Spacer(modifier = Modifier.width(10.dp))
//                Column {
//                    Text(text = "Posted by", fontSize = 12.sp, color = Color.Gray)
//                    Text(
//                        text = complaint.username.ifEmpty { "Unknown User" },
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
            // Complaint Image
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp).background(white),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                AsyncImage(
                    model = complaint.imageUri,
                    contentDescription = "Complaint Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )


            Spacer(modifier = Modifier.height(20.dp))


            Spacer(modifier = Modifier.height(15.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Date", "${complaint.dayOfWeek}, ${complaint.formattedDate}")
                    InfoRow("Time", "at ${complaint.formattedTime}")
                    InfoRow("Location", complaint.location)
                    InfoRow("Identifiable Marks", complaint.identifiableMarks)
                    InfoRow("Contact", complaint.email)

                    // Status Row
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Status",
                            tint = if (complaint.status == "Pending") Color.Red else Color.Green
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Status: ${complaint.status}",
                            fontWeight = FontWeight.Bold,
                            color = if (complaint.status == "Pending") Color.Red else Color.Green
                        )
                    }

                    if (complaint.rewards.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.reward), // Replace with actual reward icon
                                contentDescription = "Reward",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reward: ${complaint.rewards}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC107)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val uid=FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == complaint.userId) {
                        val isDelete = remember { mutableStateOf(false) }
                        val showDialog = remember { mutableStateOf(false) }

                        Button(
                            modifier = Modifier.fillMaxWidth().padding(bottom=20.dp),
                            enabled = !isDelete.value,
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(contentColor = Color.White, containerColor = secondary),
                            onClick = { showDialog.value = true }
                        ) {
                            Text(text = "Delete this complaint")
                        }

                        if (showDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showDialog.value = false },
                                title = { Text("Delete Lost Item?") },
                                text = { Text("Are you sure you want to delete this lost item post? This action cannot be undone.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showDialog.value = false
                                        isDelete.value = true
                                        coroutineScope.launch {
                                            DeleteMyComplaint(navController, complaint.postId.toString(), onSuccess = {
                                                DeleteMyImage(complaint.imageUri, onSuccess = {
                                                    Toast.makeText(context, "Post Deleted successfully", Toast.LENGTH_SHORT).show()
                                                    isDelete.value = false
                                                    navController.navigate("Home") { popUpTo(0) }
                                                }, onFailure = { isDelete.value = false })
                                            }, onFailure = {
                                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                                isDelete.value = false
                                            })
                                        }
                                    }) {
                                        Text("Yes, Delete")
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
                    else {
                        val showContactDialog = remember { mutableStateOf(false) }

                        Button(
                            modifier = Modifier.fillMaxWidth().wrapContentSize(),
                            shape = RoundedCornerShape(5.dp),
                            colors = ButtonDefaults.buttonColors(contentColor = Color.White, containerColor = primary_dark),
                            onClick = { showContactDialog.value = true }
                        ) {
                            Text(text = "I found this item")
                        }

                        if (showContactDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showContactDialog.value = false },
                                title = { Text("Contact Owner?") },
                                text = { Text("Do you want to contact the owner regarding this item?") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showContactDialog.value = false
                                        val chatId = "${uid}_${complaint.userId}"
                                        db.collection("chats").document(chatId).get().addOnSuccessListener {
                                            if(it.exists()){
                                                navController.navigate("MessageScreen/$chatId/${complaint.username}")
                                            }
                                            else{
                                                createChat(chatId, uid.toString(), complaint.userId,context)
                                                navController.navigate("ChatScreen")
                                            }
                                        }
                                    }) {
                                        Text("Yes, Contact")
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
                }
            }
        }
    }
}

// Helper function for displaying rows of information
@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "$label:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}


