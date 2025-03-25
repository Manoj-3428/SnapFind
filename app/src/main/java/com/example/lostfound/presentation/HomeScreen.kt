package com.example.lostfound.presentation

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.lostfound.model.Complaint
import com.example.lostfound.model.Profiles
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.viewmodel.ComplaintViewModel
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.collections.get
import com.example.lostfound.R
import com.example.lostfound.model.LocationDetails
import com.example.lostfound.ui.theme.green
import com.example.lostfound.ui.theme.secondary_light
import com.example.lostfound.ui.theme.white

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController, complaintViewModel: ComplaintViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val complaintList by complaintViewModel.complaintList.collectAsState()
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    val isRefreshing =complaintViewModel.isRefreshing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var profileUri: String by remember { mutableStateOf<String>("") }
    var isLoading by remember { mutableStateOf(true) }
    var auth= FirebaseAuth.getInstance()
    val keyboardController= LocalSoftwareKeyboardController.current
    var isVillageEnabled by remember { mutableStateOf(false) }
    var isDistrictEnabled by remember { mutableStateOf(false) }
    var isStateEnabled by remember { mutableStateOf(false) }
    var profile: Profiles? = null
    var locationDetails:LocationDetails?=null
    var districtName by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var villageName by remember { mutableStateOf("") }
    var user=auth.currentUser
    if(user==null){
        Toast.makeText(context,"You are loged out from your account",Toast.LENGTH_LONG).show()
        navController.navigate("login")
    }
    val db= FirebaseFirestore.getInstance()
    val userId=user?.uid
    db.collection("users").document(userId!!).get().addOnSuccessListener {
         profile=it.toObject(Profiles::class.java)
        locationDetails=profile?.locationDetails
        districtName= locationDetails?.district.toString()
        stateName=locationDetails?.state.toString()
        villageName=locationDetails?.village.toString()
    }
    LaunchedEffect(Unit) {

        isLoading = false
        complaintViewModel.fetchPosts()
    }

    Log.d("District name :",districtName)
    Log.d("State name :",stateName)
    Log.d("village name :",villageName)

    val filteredComplaints by remember(complaintList, searchQuery, isVillageEnabled, isDistrictEnabled, isStateEnabled) {
        derivedStateOf {
            val filteredBySearch = if (searchQuery.isEmpty()) {
                complaintList
            } else {
                complaintList.filter { it.username.contains(searchQuery, ignoreCase = true) }
            }

            filteredBySearch.filter { complaint ->
                (!isVillageEnabled || complaint.locationDetails.village.contains(villageName, ignoreCase = true)) &&
                        (!isDistrictEnabled || complaint.locationDetails.district!!.contains(districtName, ignoreCase = true)) &&
                        (!isStateEnabled || complaint.locationDetails.state!!.contains(stateName, ignoreCase = true))
            }
        }
    }
//    val filteredComplaints by remember {
//        derivedStateOf {
//            if (searchQuery.isEmpty()) {
//                complaintList
//            } else {
//                complaintList.filter { it.username.contains(searchQuery, ignoreCase = true) }
//            }
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary_dark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
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
    ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize().padding(paddingValues)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Complaints") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp, start = 10.dp, end = 10.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search,
                        keyboardType = KeyboardType.Text
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary_light,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = primary_light,
                        unfocusedLabelColor = Color.Gray,
                        focusedLeadingIconColor = primary_light,
                    ),

                    )
                val scrollState= rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth().horizontalScroll(scrollState)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {


                    // Village Button
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(125.dp)
                            .padding(start = 10.dp, end = 10.dp)
                            .background(
                                color = if (isVillageEnabled) Color.Gray else primary_dark,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                isVillageEnabled = !isVillageEnabled // Toggle village filter
                            }
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Village",
                            color = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

// District Button
                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(125.dp)
                            .padding(start = 10.dp, end = 10.dp)
                            .background(
                                color = if (isDistrictEnabled) Color.Gray else primary_dark,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                isDistrictEnabled = !isDistrictEnabled // Toggle district filter
                            }
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "District",
                            color = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(45.dp)
                            .width(125.dp)
                            .padding(start = 10.dp, end = 10.dp)
                            .background(
                                color = if (isStateEnabled) Color.Gray else primary_dark,
                                shape = RoundedCornerShape(0.dp)
                            )
                            .clickable {
                                isStateEnabled = !isStateEnabled // Toggle state filter
                            }
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "State",
                            color = Color.White,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
                if (isLoading) {
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement =Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primary_dark, modifier = Modifier.padding(bottom = 20.dp))
                        Text("Complaints are fetching please wait")
                    }
                } else {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(isRefreshing.value),
                        onRefresh = {
                            complaintViewModel.fetchPosts()
                        }
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(filteredComplaints.size) { index ->
                                ComplaintItem(filteredComplaints[index]) {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "complaint",
                                        filteredComplaints[index]
                                    )
                                    navController.navigate("DetailScreen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
@Composable
fun ComplaintItem(complaint: Complaint, onclick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clickable { onclick() }
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White, Color.LightGray),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f)
                ),
                shape = RoundedCornerShape(0.dp)
            ),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    )
    {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(secondary_light),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = complaint.profileUri.takeIf { it.isNotEmpty() }
                            ?: R.drawable.avatar,
                        contentDescription = "User Profile Image",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = complaint.username.ifEmpty { "Unknown User" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = complaint.imageUri.ifEmpty { R.drawable.post },
                    contentDescription = "Complaint Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "${complaint.dayOfWeek}, ${complaint.formattedDate}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "at ${complaint.formattedTime}",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
                        Text(text = "Status: ", fontWeight = FontWeight.Bold)
                        Text(
                            text = complaint.status,
                            color = if (complaint.status == "Pending") Color.Red else Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (complaint.rewards.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
                            Icon(
                                painter = painterResource(R.drawable.reward),
                                contentDescription = "Reward",
                                tint = green,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Reward: ${complaint.rewards}",
                                fontWeight = FontWeight.Bold,
                                color = green,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }

            }
        }
    }
}
