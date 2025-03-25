package com.example.lostfound

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.lostfound.model.Complaint
import com.example.lostfound.presentation.CallScreen
import com.example.lostfound.presentation.ChatScreen
import com.example.lostfound.presentation.ComplaintScreen
import com.example.lostfound.presentation.DetailScreen
import com.example.lostfound.presentation.Home
import com.example.lostfound.presentation.MessageScreen
import com.example.lostfound.presentation.addComplaint
import com.example.lostfound.presentation.animation.LottieAnimation
import com.example.lostfound.presentation.authentication.LoginScreen
import com.example.lostfound.presentation.authentication.SignupScreen
import com.example.lostfound.presentation.onboarding.OnboardingScreen
import com.example.lostfound.presentation.profile
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.white
import com.example.lostfound.viewmodel.AuthViewModel
import com.example.lostfound.viewmodel.ComplaintViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val complaintViewModel: ComplaintViewModel = viewModel()
            val navController = rememberNavController()
            var showTopBar by remember { mutableStateOf(false) }
            var showBottomBar by remember { mutableStateOf(false) }
            var route by remember { mutableStateOf("home") }
            val authViewModel: AuthViewModel = viewModel()
            LaunchedEffect(Unit) {
                val complaintId = intent?.getStringExtra("complaintId")
                if (!complaintId.isNullOrEmpty()) {
                    navController.navigate("DetailScreen/$complaintId")
                }
            }
            LaunchedEffect(Unit) {
                authViewModel.updateFcmToken()
            }

            Scaffold(
                topBar = { if (showTopBar) AppTopBar(route) },
                bottomBar = { if (showBottomBar) AppBottomBar(navController) }
            ) { paddingValues ->
                NavHost(
                    navController,
                    startDestination = "home",
                    modifier = Modifier.padding()
                ) {
                    composable("onboarding") {
                        showTopBar = false
                        showBottomBar = false
                        OnboardingScreen(navController)
                    }
                    composable("login") {
                        showTopBar = false
                        showBottomBar = false
                        LoginScreen(navController)
                    }
                    composable("signup") {
                        showTopBar = false
                        showBottomBar = false
                        SignupScreen(navController)
                    }
                    composable("home") {
                        showTopBar = false
                        showBottomBar = true
                        route="home"
                        Home(navController, complaintViewModel)
                    }
                    composable("ChatScreen") {
                        showTopBar = false
                        showBottomBar = true
                        route="chats"
                        ChatScreen(navController = navController)
                    }
                    composable("MessageScreen/{chatId}/{otheridName}") { backStackEntry ->
                        showTopBar = false
                        showBottomBar = false
                        route="message"
                        val chatId = backStackEntry.arguments?.getString("chatId")
                        val otheridName = backStackEntry.arguments?.getString("otheridName")
                        if (chatId != null) {
                            MessageScreen(otheridName.toString(),chatId = chatId, navController = navController)
                        }
                    }
                    composable("profile") {
                        profile(navController)
                    }
                    composable("calls") {
                        showTopBar = true
                        showBottomBar = true
                        CallScreen(navController)
                    }
                    composable("addComplaint") {
                        showTopBar = false
                        showBottomBar = false
                        addComplaint(navController)
                    }
                    composable("LottieAnimation") {
                        LottieAnimation()
                    }
                    composable("DetailScreen") {
                        showTopBar = false
                        showBottomBar = false
                        val complaint = navController.previousBackStackEntry?.savedStateHandle?.get<Complaint>("complaint")
                        DetailScreen(complaint, navController)
                    }
                    composable("DetailScreen/{complaintId}") { backStackEntry ->
                        val complaintId = backStackEntry.arguments?.getString("complaintId")
                        val complaint by complaintViewModel.getComplaintById(complaintId).collectAsState(initial = null) // Fetch complaint
                        DetailScreen(complaint, navController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(route: String) {
    if(route=="home") {
        TopAppBar(
            title = { Text(text = "SnapFind", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = primary_dark),
            actions = {
                IconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
            }
        )
    }
    else if(route=="chats"){
        TopAppBar(
            title = { Text(text = "Chats", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = primary_dark),
            actions = {
                IconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
            }
        )
    }
    else if(route=="message"){
        TopAppBar(
            title = { Text(text = "Chats", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = primary_dark),
            actions = {
                IconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
            }
        )
    }
}

@Composable
fun AppBottomBar(navController: NavController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(containerColor = primary_dark) {
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentDestination == "home") Color.Black else Color.White
                )
            },
            label = { Text("Home", color = white) },
            selected = currentDestination == "home",
            onClick = { navController.navigate("home") }
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentSize(),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("addComplaint") },
                containerColor = Color.White,
                contentColor = primary_dark,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "addComplaint")
            }
        }
        NavigationBarItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    painterResource(R.drawable.messages),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Chats",
                    tint = if (currentDestination == "ChatScreen") Color.Black else Color.White
                )
            },
            label = { Text("Chats", color = white) },
            selected = currentDestination == "ChatScreen",
            onClick = { navController.navigate("ChatScreen") } // Correct route
        )
    }
}
private fun updateFcmToken(newToken: String) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid

    if (uid != null) {
        db.collection("users").document(uid)
            .update("fcmToken", newToken)
            .addOnSuccessListener {
                Log.d("TAG", "FCM token updated successfully")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Failed to update FCM token", e)
            }
    }
}


