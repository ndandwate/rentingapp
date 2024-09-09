package com.example.myrentingapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
//Renter Login screen
@Composable
fun RenterLogin(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var emailSentMessage by remember { mutableStateOf<String?>(null) }
    var passwordValidationError by remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.signup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer(translationY = -140f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = "Welcome Back",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "RENTER",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Magenta
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
                    .graphicsLayer(
                        scaleX = 0.8f,
                        scaleY = 0.8f
                    )
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        painterResource(id = R.drawable.visible)
                    else
                        painterResource(R.drawable.invisible)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Image(
                            painter = icon,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    passwordValidationError = if (password.isBlank()) {
                        "Password cannot be blank."
                    } else {
                        null
                    }
                    if (passwordValidationError == null) {
                        signInWithEmailAndPassword(email, password, navController) { error ->
                            loginError = error
                            emailSentMessage = null
                        }
                    }
                },
                modifier = Modifier.width(240.dp)
            ) {
                Box(
                    modifier = Modifier.width(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Forgot Password?",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                ),
                modifier = Modifier.clickable {
                    if (email.isBlank()) {
                        emailSentMessage = "Please enter your email address."
                    } else {
                        sendPasswrdResetEmail(email) { message ->
                            emailSentMessage = message
                            loginError = null
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Switch to Rentee Page?",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                ),
                modifier = Modifier.clickable {
                    navController.navigate("renteelogin")
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            loginError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            emailSentMessage?.let {
                Text(
                    text = it,
                    color = if (it == "Please enter your email address.") Color.Red else Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            passwordValidationError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                )
                Text(
                    text = "Sign up",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    ),
                    modifier = Modifier.clickable { navController.navigate("rentersignup") }
                )
            }
        }
    }
}
fun signInWithEmailAndPassword(
    email: String,
    password: String,
    navController: NavController,
    onError: (String) -> Unit
) {
    val auth = Firebase.auth

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("upload_screen")
            } else {
                val errorMessage = when (task.exception?.message) {
                    "The email address is badly formatted." -> "Invalid email address format."
                    "There is no user record corresponding to this identifier. The user may have been deleted." -> "Account with this email ID does not exist."
                    "The password is invalid or the user does not have a password." -> "Incorrect password."
                    else -> "Email ID or password is incorrect. Please try again."
                }
                onError(errorMessage)
            }
        }
}
fun sendPasswrdResetEmail(
    email: String,
    onResult: (String) -> Unit
) {
    val auth = Firebase.auth
    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult("Password reset email sent. Check your inbox.")
            } else {
                val errorMessage = task.exception?.message ?: "Error sending password reset email."
                onResult(errorMessage)
            }
        }
}

