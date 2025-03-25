package com.example.lostfound.presentation.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lostfound.R
import com.example.lostfound.ui.theme.primary
import com.example.lostfound.ui.theme.primary_dark
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class Onboarding(val image: Int, val title: String, val desc: String)
val pages = listOf(
    Onboarding(R.drawable.location, "Search for Lost Items", "Easily browse lost items reported by others."),
    Onboarding(R.drawable.upload, "Report a Lost Item", "Upload details and a photo to help find your lost belongings."),
    Onboarding(R.drawable.chat, "Connect with Finders", "Message the person who found your item securely within the app."),
    Onboarding(R.drawable.community, "Community-Powered Recovery", "Together, let's reunite lost items with their rightful owners!")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()
    val auth= FirebaseAuth.getInstance()
    if(auth.currentUser==null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentPage = pages[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = currentPage.image),
                        contentDescription = null,
                        modifier = Modifier.size(280.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = currentPage.title,
                        fontSize = 24.sp,
                        color = primary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentPage.desc,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(pages.size) { index ->
                    val color =
                        if (index == pagerState.currentPage) primary_dark else Color.LightGray
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(color)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = if (pagerState.currentPage == pages.size - 1) Arrangement.Center else Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage < pages.size - 1) {
                    TextButton(onClick = { navController.navigate("login"){popUpTo(0)} }) {
                        Text("Skip", fontSize = 18.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (pagerState.currentPage < pages.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                navController.navigate("login")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary_dark),
                    shape = RoundedCornerShape(5.dp),
                    modifier = if (pagerState.currentPage == pages.size - 1) Modifier.width(250.dp) else Modifier.wrapContentSize()
                ) {
                    Text(
                        if (pagerState.currentPage == pages.size - 1) "Get Started" else "Next",
                        color = Color.White
                    )
                }
            }
        }
    }
    else{
        navController.navigate("login")
    }
}