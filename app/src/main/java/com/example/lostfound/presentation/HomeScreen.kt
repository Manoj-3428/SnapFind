package com.example.lostfound.presentation

import android.graphics.Color.alpha
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController, complaintViewModel: ComplaintViewModel) {
    val context = LocalContext.current
    val complaintList by complaintViewModel.complaintList.collectAsState()
    val isRefreshing = complaintViewModel.isRefreshing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isVillageEnabled by remember { mutableStateOf(false) }
    var isDistrictEnabled by remember { mutableStateOf(false) }
    var isStateEnabled by remember { mutableStateOf(false) }

    // User location data
    var districtName by remember { mutableStateOf("") }
    var stateName by remember { mutableStateOf("") }
    var villageName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var userProfile by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user == null) {
        Toast.makeText(context, "You are logged out from your account", Toast.LENGTH_LONG).show()
        navController.navigate("login"){
            popUpTo(0)
        }
        return
    }

    val db = FirebaseFirestore.getInstance()
    val userId = user.uid

    LaunchedEffect(userId) {
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            document.toObject(Profiles::class.java)?.let { profile ->
                userProfile=profile.uri
                profile.locationDetails?.let { location ->
                    districtName = location.district ?: ""
                    stateName = location.state ?: ""
                    villageName = location.village ?: ""

                }
            }
        }
        delay(500) // Small delay to ensure data is loaded
        complaintViewModel.fetchPosts()
        isLoading = false
    }

    val filteredComplaints by remember(complaintList, searchQuery, isVillageEnabled, isDistrictEnabled, isStateEnabled, villageName, districtName, stateName) {
        derivedStateOf {
            val filteredBySearch = if (searchQuery.isEmpty()) {
                complaintList
            } else {
                complaintList.filter {
                    it.username.contains(searchQuery, ignoreCase = true) ||
                            it.username .contains(searchQuery, ignoreCase = true)
                }
            }

            filteredBySearch.filter { complaint ->
                (!isVillageEnabled || complaint.locationDetails.village.contains(villageName, ignoreCase = true)) &&
                        (!isDistrictEnabled || complaint.locationDetails.district?.contains(districtName, ignoreCase = true) == true) &&
                        (!isStateEnabled || complaint.locationDetails.state?.contains(stateName, ignoreCase = true) == true)
            }.sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Snap & Find",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary_dark,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { navController.navigate("profile") }
                    ) {

                        AsyncImage(
                            model = if (userProfile.isEmpty()) R.drawable.avatar else userProfile,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            )
        },
        bottomBar = {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
            NavigationBar(
                containerColor = primary_dark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (currentDestination == "home") primary_light else Color.White
                        )
                    },
                    label = {
                        Text(
                            "Home",
                            color = if (currentDestination == "home") primary_light else Color.White
                        )
                    },
                    selected = currentDestination == "home",
                    onClick = { navController.navigate("home") }
                )

                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(primary_light, CircleShape)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp))
                        }
                    },
                    label = { Text("Add", color = Color.White) },
                    selected = false,
                    onClick = { navController.navigate("addComplaint") }
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(R.drawable.messages),
                            contentDescription = "Chats",
                            tint = if (currentDestination == "ChatScreen") primary_light else Color.White
                        )
                    },
                    label = {
                        Text(
                            "Chats",
                            color = if (currentDestination == "ChatScreen") primary_light else Color.White
                        )
                    },
                    selected = currentDestination == "ChatScreen",
                    onClick = { navController.navigate("ChatScreen") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                           },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FilterChipsRow(
                isVillageEnabled = isVillageEnabled,
                isDistrictEnabled = isDistrictEnabled,
                isStateEnabled = isStateEnabled,
                onVillageFilterChange = { isVillageEnabled = it },
                onDistrictFilterChange = { isDistrictEnabled = it },
                onStateFilterChange = { isStateEnabled = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading) {
                LoadingIndicator()
            } else {
                ComplaintList(
                    complaints = filteredComplaints,
                    isRefreshing = isRefreshing.value,
                    onRefresh = { complaintViewModel.fetchPosts() },
                    onComplaintClick = { complaint ->
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "complaint",
                            complaint
                        )
                        navController.navigate("DetailScreen")
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = {
            Text(
                "Search complaints...",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = true,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = primary_light,
            unfocusedBorderColor = Color.LightGray,
            focusedLabelColor = primary_light,
            unfocusedLabelColor = Color.Gray,
            cursorColor = primary_light,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        leadingIcon = {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.magnifier),
                contentDescription = "Search",
                tint = Color.Unspecified
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.Unspecified
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()

            }
        )
    )
}

@Composable
fun FilterChipsRow(
    isVillageEnabled: Boolean,
    isDistrictEnabled: Boolean,
    isStateEnabled: Boolean,
    onVillageFilterChange: (Boolean) -> Unit,
    onDistrictFilterChange: (Boolean) -> Unit,
    onStateFilterChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier.horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip(
            selected = isVillageEnabled,
            modifier = Modifier.padding(start = 5.dp,end=5.dp),
            onClick = { onVillageFilterChange(!isVillageEnabled) },
            label = { Text("Village",fontSize = 14.sp) },
            leadingIcon = if (isVillageEnabled) {
                {
                    Icon(
                        painter = painterResource(R.drawable.magnifier),
                        contentDescription = "Selected",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = primary_light.copy(alpha = 0.2f),
                selectedLabelColor = primary_dark,
                selectedLeadingIconColor = primary_dark
            )
        )

        FilterChip(
            selected = isDistrictEnabled,
            modifier = Modifier.padding(start = 5.dp,end=5.dp),
            onClick = { onDistrictFilterChange(!isDistrictEnabled) },
            label = { Text("District",fontSize = 14.sp) },
            leadingIcon = if (isDistrictEnabled) {
                {
                    Icon(
                        painter = painterResource(R.drawable.magnifier),
                        contentDescription = "Selected",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = primary_light.copy(alpha = 0.2f),
                selectedLabelColor = primary_dark,
                selectedLeadingIconColor = primary_dark
            )
        )

        FilterChip(
            selected = isStateEnabled,
            modifier = Modifier.padding(start = 5.dp,end=5.dp),
            onClick = { onStateFilterChange(!isStateEnabled) },
            label = { Text("State",fontSize = 14.sp) },
            leadingIcon = if (isStateEnabled) {
                {
                    Icon(
                        painter = painterResource(R.drawable.magnifier),
                        contentDescription = "Selected",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = primary_light.copy(alpha = 0.2f),
                selectedLabelColor = primary_dark,
                selectedLeadingIconColor = primary_dark
            )
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = primary_dark)
            Text(
                text = "Loading complaints...",
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ComplaintList(
    complaints: List<Complaint>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onComplaintClick: (Complaint) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        if (complaints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No complaints found",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(complaints.size) { index ->
                    ComplaintCard(
                        complaint = complaints[index],
                        onClick = { onComplaintClick(complaints[index]) }
                    )
                }
            }
        }
    }
}

@Composable
fun ComplaintCard(complaint: Complaint, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = complaint.profileUri.takeIf { it.isNotEmpty() } ?: R.drawable.avatar,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(
                        text = complaint.username.ifEmpty { "Anonymous" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${complaint.formattedDate} • ${complaint.formattedTime}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (complaint.status) {
                                "Resolved" -> green.copy(alpha = 0.2f)
                                else -> Color.Red.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition()
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.7f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    val textColor = if (complaint.status == "Resolved") Color(0xFF2E7D32) else Color(0xFFC62828)
                    val bgColor = if (complaint.status == "Resolved") Color(0xFF81C784) else Color(0xFFFFCDD2)
                    Text(
                        text ="${complaint.type} is missing!!" ,
                        color = when (complaint.status) {
                            "Resolved" -> green
                            else -> Color.Red
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Complaint image
            AsyncImage(
                model = complaint.imageUri.ifEmpty { R.drawable.post },
                contentDescription = "Complaint",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Complaint details
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {


                Text(
                    text = complaint.identifiableMarks .take(100) + if (complaint.identifiableMarks.length > 100) "..." else "",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.marker),
                            contentDescription = "Location",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = complaint.locationDetails.village,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    if (complaint.rewards.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.reward),
                                contentDescription = "Reward",
                                tint = green,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = complaint.rewards,
                                fontSize = 12.sp,
                                color = green,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}