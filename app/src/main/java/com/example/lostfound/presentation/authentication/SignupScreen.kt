package com.example.lostfound.presentation.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lostfound.R
import com.example.lostfound.ui.theme.primary
import com.example.lostfound.ui.theme.primary_dark
import com.example.lostfound.ui.theme.primary_light
import com.example.lostfound.ui.theme.secondary_light
import com.example.lostfound.viewmodel.createUser
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(navController: NavController) {
    val emailTextState = remember { mutableStateOf("") }
    val passwordTextState = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    val focus = remember { mutableStateOf(Color.Black) }
    val labelFocus=remember { mutableStateOf(Color.Black) }
    val nameState=remember { mutableStateOf("") }
    val context=LocalContext.current
    Scaffold(modifier = Modifier.fillMaxSize()) {it->
        Column(
            modifier = Modifier.padding(it).fillMaxSize().imePadding().verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.upload),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().size(200.dp), contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "SnapFind",
                style = MaterialTheme.typography.headlineMedium, color = primary_light
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Create your account",
                style = MaterialTheme.typography.titleLarge, color = primary_dark
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nameState.value,
                onValueChange = { nameState.value = it },
                label = {
                    Text(text = "Name", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.plus_black),
                        contentDescription = "mail box", modifier = Modifier.size(24.dp)
                    )

                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.wrapContentSize().fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = focus.value,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = labelFocus.value,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = focus.value
                )
            )
            OutlinedTextField(
                value = emailTextState.value,
                onValueChange = { emailTextState.value = it },
                label = {
                    Text(text = "Email", color = Color.Gray)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.mail),
                        contentDescription = "mail box", modifier = Modifier.size(24.dp)
                    )

                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.wrapContentSize().fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, top = 10.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedBorderColor = focus.value,
                    focusedLabelColor = secondary_light,
                    unfocusedLabelColor = labelFocus.value,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = focus.value
                )
            )
            OutlinedTextField(
                value = passwordTextState.value,
                onValueChange = { passwordTextState.value = it },
                label = { Text(text = "password", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier.wrapContentSize().fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, top = 10.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.lock),
                        contentDescription = "Lock"
                    )
                },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = secondary_light,
                    unfocusedLabelColor = focus.value,
                    focusedLabelColor = secondary_light,
                    unfocusedBorderColor = labelFocus.value,
                    focusedLeadingIconColor = secondary_light,
                    unfocusedLeadingIconColor = focus.value
                )
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (emailTextState.value.isEmpty() || passwordTextState.value.isEmpty()) {
                        focus.value= Color.Red
                        labelFocus.value=Color.Red
                    } else {
                        createUser(
                            context,
                            nameState.value,
                            emailTextState.value,
                            passwordTextState.value
                        ) {
                            if (it == "Success") {
                                navController.navigate("home")
                            } else {
                                Log.w("Error : ", it)
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 50.dp, end = 50.dp, top = 5.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = colorResource(R.color.white)
                )
            ) {
                Text(text = "Create Account")
            }
            Text(
                text = "Already have an account?",
                color = colorResource(R.color.black),
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Monospace,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp).clickable(onClick = {
                    navController.navigate("login")
                })
            )
            Text(
                text = "Powered by Manoj",
                color = secondary_light,
                modifier = Modifier.padding(top = 5.dp, bottom = 2.dp)
            )

        }
    }
    LaunchedEffect(focus.value) {
        if (focus.value == Color.Red) {
            delay(3000)
            focus.value = Color.Black
            labelFocus.value=Color.Black
        }
    }
}
