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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
//Renter Signup Screen
@Composable
fun RenterSignup(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+44") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var userCreationError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.signup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .graphicsLayer(translationY = -150f)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(90.dp))
            Text(
                text = "Welcome",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "RENTER",
                style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Magenta
                ),
            )
            Spacer(modifier = Modifier.height(5.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = email,
                onValueChange = { newEmail ->
                    email = newEmail
                    emailError = null
                },
                label = { Text("Email") },
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = phone,
                onValueChange = {
                    if (it.startsWith("+44") && it.drop(3).all { char -> char.isDigit() } && it.length <= 14) {
                        phone = it
                        phoneError = it.length != 13
                    }
                },
                label = { Text("Phone") },
                modifier = Modifier
                    .width(240.dp)
                    .padding(vertical = 8.dp)
            )
            TextField(
                value = password,
                onValueChange = { password = it
                    if (password.length >= 6) {
                        userCreationError = null
                    }  },
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
            Spacer(modifier = Modifier.height(1.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                emailError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (phoneError) {
                    Text(
                        text = "Phone number must be 10 digits",
                        color = Color.Red,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (nameError) {
                    Text(
                        text = "Name is required",
                        color = Color.Red,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                userCreationError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            Button(
                onClick = {
                    nameError = name.isEmpty()
                    emailError = if (email.isEmpty()) "Email is required" else null
                    phoneError = phone.length != 13 || !phone.startsWith("+44")
                    if (nameError || emailError != null || phoneError) {
                        userCreationError = "Please fill out all fields correctly."
                    } else if (password.length < 6) {
                        userCreationError = "Password must be at least 6 characters."
                    } else {
                        checkIfUsrExist(email, phone) { exists, message ->
                            if (exists) {
                                emailError = message
                            } else {
                                createUserWithEmailAndPassword(email, password, name, phone, address, navController) { error ->
                                    userCreationError = error
                                }
                            }
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
                        text = "Sign Up",
                        modifier = Modifier.width(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(35.dp))
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                )
                Text(
                    text = "Login",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Blue
                    ),
                    modifier = Modifier.clickable { navController.navigate("renterlogin") }
                )
            }
        }
    }
}
fun checkIfUsrExist(email: String, phone: String, onCheckComplete: (Boolean, String?) -> Unit) {
    val database = Firebase.database
    val usersRef = database.reference.child("users")

    usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
        ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.exists()) {
                onCheckComplete(true, "An account with this email already exists.")
            } else {
                usersRef.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            onCheckComplete(true, "An account with this phone number already exists.")
                        } else {
                            onCheckComplete(false, null)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        onCheckComplete(false, databaseError.message)
                    }
                })
            }
        }
        override fun onCancelled(databaseError: DatabaseError) {
            onCheckComplete(false, databaseError.message)
        }
    })
}

fun createUserWithEmailAndPassword(
    email: String,
    password: String,
    name: String,
    phone: String,
    address: String,
    navController: NavController,
    onError: (String) -> Unit
) {
    val auth = Firebase.auth
    val database = Firebase.database

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val user = User(name, email, phone, address)
                    database.reference.child("users").child(userId).setValue(user)
                        .addOnSuccessListener {
                            navController.navigate("upload_screen")
                        }
                        .addOnFailureListener { exception ->
                            onError(exception.message ?: "Error saving user details")
                        }
                }
            } else {
                onError(task.exception?.message ?: "Error creating user")
            }
        }
}
