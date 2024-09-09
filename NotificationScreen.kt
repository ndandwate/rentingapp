package com.example.myrentingapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import java.net.URLEncoder
//screen to control all the notifications
@Composable
fun NotificationScreen(navController: NavController) {
    val notifications = remember { mutableStateListOf<Notification>() }
    var showResponseDialog by remember { mutableStateOf(false) }
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
    if (currentUserEmail != null) {
        val dbRef = FirebaseDatabase.getInstance().reference.child("notifications")
        LaunchedEffect(Unit) {
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    notifications.clear()
                    val currentTime = System.currentTimeMillis()
                    for (child in snapshot.children) {
                        val notification = child.getValue(Notification::class.java)
                        notification?.let {
                            if (it.recipientEmail == currentUserEmail) {
                                if (currentTime - it.timestamp <= 24 * 60 * 60 * 1000) {
                                    notifications.add(it)
                                } else {
                                    child.ref.removeValue()
                                }
                            }
                        }
                    }

                    notifications.sortByDescending { it.timestamp }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
    Scaffold()
    { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.notification),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp).align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        navController = navController,
                        onApprove = {
                            handleApproval(notification)
                        },
                        onReject = {
                            handleRejection(notification)
                        },
                        onUploadImages = { message, requesterEmail ->
                            val encodedRequesterEmail =
                                URLEncoder.encode(requesterEmail ?: "", "UTF-8")
                            navController.navigate("photography/$message/$encodedRequesterEmail")
                        },
                        onViewPhotos = {
                            navController.navigate("viewphotos")
                        },
                        onSendResponse = {
                            selectedNotification = notification
                            showResponseDialog = true
                        }
                    )
                }
            }
        }
        if (showResponseDialog) {
            ResponseDialog(
                onDismiss = { showResponseDialog = false },
                onSend = { response ->
                    selectedNotification?.let {
                        sendResponseToRequester(it.requesterEmail, response)
                        showResponseDialog = false
                    }
                }
            )
        }
    }
}
@Composable
fun ResponseDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var responseText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Your Response") },
        text = {
            Column {
                BasicTextField(
                    value = responseText,
                    onValueChange = { responseText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    singleLine = false
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (responseText.isNotEmpty()) {
                        onSend(responseText)
                        responseText = ""
                    }
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationCard(
    notification: Notification,
    navController: NavController,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onUploadImages: (String, String?) -> Unit,
    onViewPhotos: () -> Unit,
    onSendResponse: () -> Unit
) {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(notification.title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
            if (currentUserEmail == notification.recipientEmail && notification.showActions) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = onApprove) {
                        Text("Approve")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onReject) {
                        Text("Reject")
                    }
                }
            }
            else if(notification.status == "approved"){
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val approverEmail = notification.approverEmail ?: ""
                        navController.navigate("ownerDetails/${notification.message}/${approverEmail}")
                    }
                ) {
                    Text("View Details")
                }
            }
            if (notification.type == "upload_request" && currentUserEmail == notification.recipientEmail) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onUploadImages(notification.message,notification.requesterEmail) }) {
                    Text("Upload Images")
                }
            }
            if (notification.type == "VIEW_PHOTOS") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onViewPhotos() }) {
                    Text("View Photos")
                }
            }
            if (notification.type == "chat_message") {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onSendResponse() }) {
                    Text("Send Your Response")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Received on: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(notification.timestamp)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
fun sendResponseToRequester(requesterEmail: String?, responseText: String) {
    if (requesterEmail.isNullOrBlank()) {
        Log.e("NotificationScreen", "Requester email is null or blank.")
        return
    }

    try {
        val responseId = FirebaseDatabase.getInstance().reference.push().key ?: ""
        val responseData = Notification(
            title = "Response from Owner",
            message = responseText,
            recipientEmail = requesterEmail,
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
                Log.d("NotificationScreen", "Response sent successfully to $requesterEmail")
            }
            .addOnFailureListener { e ->
                Log.e("NotificationScreen", "Error sending response to $requesterEmail: ${e.message}", e)
            }
    } catch (e: Exception) {
        Log.e("NotificationScreen", "Exception occurred during response operation: ${e.message}", e)
    }
}

fun handleApproval(notification: Notification) {
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
    val dbRef = FirebaseDatabase.getInstance().reference.child("notifications").child(notification.notificationId)
    dbRef.child("status").setValue("approved")
    dbRef.child("approverEmail").setValue(currentUserEmail)
    val requesterEmail = notification.requesterEmail
    val requesterNotificationId = FirebaseDatabase.getInstance().reference.push().key ?: ""
    val approvalNotification = Notification(
        title = "Request Approved!",
        message = """
            Dear Customer,

            Your request has been approved.

            Click on the button below to view details.
        """.trimIndent(),
        recipientEmail = requesterEmail,
        timestamp = System.currentTimeMillis(),
        showActions = false,
        notificationId = requesterNotificationId,
        status = "approved",
        approverEmail = currentUserEmail
    )
    val requesterDbRef = FirebaseDatabase.getInstance().reference.child("notifications")
    requesterDbRef.child(requesterNotificationId).setValue(approvalNotification)
}

fun handleRejection(notification: Notification) {
    val dbRef = FirebaseDatabase.getInstance().reference.child("notifications").child(notification.notificationId)
    dbRef.child("status").setValue("rejected")
    val requesterNotificationId = FirebaseDatabase.getInstance().reference.push().key ?: ""
    val rejectionNotification = Notification(
        title = "Request Rejected",
        message = "Your request for details on ${notification.message} has been rejected.",
        recipientEmail = notification.requesterEmail,
        timestamp = System.currentTimeMillis(),
        showActions = false,
        notificationId = requesterNotificationId,
        status = "rejected"
    )
    val requesterDbRef = FirebaseDatabase.getInstance().reference.child("notifications")
    requesterDbRef.child(requesterNotificationId).setValue(rejectionNotification)
}
