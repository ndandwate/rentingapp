package com.example.myrentingapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
//Function to let user chat/message the ownwer and owner can respond back to user
@Composable
fun ChatScreen(
    navController: NavController,
    ownerEmail: String?,
    userEmail: String?
) {
    var messageText by remember { mutableStateOf("") }
    var showResponseDialog by remember { mutableStateOf(false) }
    var responseText by remember { mutableStateOf("") }
    val currentUserEmail by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.email) }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.productbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(top = 56.dp)
        ) {
            Text(
                text = "Enter a message for owner:",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.Transparent)
                        .padding(16.dp),
                    singleLine = false
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.Center)
            ) {
                Button(
                    onClick = {
                        if (messageText.isNotEmpty()) {
                            notifyOwner(ownerEmail, userEmail, messageText)
                            messageText = ""
                        }
                    }
                ) {
                    Text("Submit")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (currentUserEmail == ownerEmail)
            {
                Button(
                    onClick = { showResponseDialog = true },
                    modifier = Modifier.align(Alignment.End)
                )
                {
                    Text("Enter Your Response")
                }
            } else
            {
                Log.d("ChatScreen", "Button not shown because currentUserEmail does not match ownerEmail")
            }
            if (showResponseDialog)
            {
                AlertDialog(
                    onDismissRequest = { showResponseDialog = false },
                    title = { Text("Enter Your Response") },
                    text = {
                        Column {
                            BasicTextField(
                                value = responseText,
                                onValueChange = { responseText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (responseText.isNotEmpty()) {
                                    submitResponse(ownerEmail, responseText)
                                    responseText = ""
                                    showResponseDialog = false
                                }
                            }
                        ) {
                            Text("Submit")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showResponseDialog = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
fun notifyOwner(ownerEmail: String?, userEmail: String?, message: String) {
    if (ownerEmail.isNullOrBlank() || userEmail.isNullOrBlank()) {
        Log.e("ChatScreen", "Owner email or user email is null or blank.")
        return
    }

    try {
        val notificationId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val notificationData = Notification(
            title = "New Message from $userEmail",
            message = message,
            recipientEmail = ownerEmail,
            timestamp = System.currentTimeMillis(),
            showActions = false,
            notificationId = notificationId,
            status = "new",
            type = "chat_message",
            requesterEmail = userEmail
        )
        FirebaseDatabase.getInstance()
            .reference
            .child("notifications")
            .child(notificationId)
            .setValue(notificationData)
            .addOnSuccessListener {
                Log.d("ChatScreen", "Notification sent successfully to $ownerEmail")
            }
            .addOnFailureListener { e ->
                Log.e("ChatScreen", "Error sending notification to $ownerEmail: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e("ChatScreen", "Exception occurred during notification operation: ${e.message}", e)
    }
}
fun submitResponse(ownerEmail: String?, responseText: String) {
    if (ownerEmail.isNullOrBlank())
    {
        Log.e("ChatScreen", "Owner email is null or blank.")
        return
    }
    try {
        val responseId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val responseData = Notification(
            title = "Response from Owner",
            message = responseText,
            recipientEmail = ownerEmail,
            timestamp = System.currentTimeMillis(),
            showActions = false,
            notificationId = responseId,
            status = "new",
            type = "response"
        )
        FirebaseDatabase.getInstance()
            .reference
            .child("notifications")
            .child(responseId)
            .setValue(responseData)
            .addOnSuccessListener {
                Log.d("ChatScreen", "Response sent successfully to $ownerEmail")
            }
            .addOnFailureListener { e ->
                Log.e("ChatScreen", "Error sending response to $ownerEmail: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e("ChatScreen", "Exception occurred during response operation: ${e.message}", e)
    }
}
