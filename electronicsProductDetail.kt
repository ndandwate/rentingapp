package com.example.myrentingapplication

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.google.firebase.database.*
import kotlinx.coroutines.launch

import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import com.google.firebase.auth.FirebaseAuth
//Function to give details of individual product and option to interact with owner via requesting more product photos, getting owner details,message owner
@Composable
fun electronicsProductDetail(productName: String, navController: NavController, user:User) {
    var product by remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var showMenu by remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val photoDialog = remember { mutableStateOf(false) }
    val showConfirmationDialog = remember { mutableStateOf(false) }
    val purposeText = remember { mutableStateOf("") }
    var currentUserEmail by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(productName) {
        coroutineScope.launch {
            fetchProductDetailFromFirebase(productName) { fetchedProduct, error ->
                if (fetchedProduct != null) {
                    product = fetchedProduct
                    currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
                } else {
                }
                loading = false
            }
        }
    }
    if (loading) {
        Text("Loading...")
    } else {
        product?.let {
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
                ) {
                   Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { navController.popBackStack() }
                        )
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.Black,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { showMenu = !showMenu }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.profile),
                            contentDescription = "Profile Icon",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it.ownerName,
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            val imageData = parseImageData(it.productImage)
                            when (imageData) {
                                is ImageData.Base64Image -> {
                                    imageData.base64String.decodeBase64()?.let { imageBitmap ->
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = "Product Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 16.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                is ImageData.UrlImage -> {
                                    val painter = rememberImagePainter(data = imageData.url)
                                    Image(
                                        painter = painter,
                                        contentDescription = "Product Image",
                                        modifier = Modifier
                                            .size(300.dp)
                                            .padding(end = 16.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = it.productName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "${it.category}",
                                fontSize = 16.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Description:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            val descriptionLines = it.productDescription.split("\n", ",").map { line -> line.trim() }
                            descriptionLines.forEach { line ->
                                Text(
                                    text = line,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "Product Type: ",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = it.productType,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "Minimal Rental Period: ",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${it.minimalRentalPeriod}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "Price Per Day: ",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "£${it.pricePerDay}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "£${it.pricePerDay}/day (min ${it.minimalRentalPeriod})",
                            fontSize = 16.sp,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        Button(onClick = {  navController.navigate("rent/${it.pricePerDay}/${it.minimalRentalPeriod}/${it.ownerEmail}/${it.productName}") })
                        {
                            Text("Rent")
                        }
                    }
                }
                AnimatedVisibility(
                    visible = showMenu,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 10.dp, top = 50.dp)
                            .wrapContentSize()
                            .align(Alignment.TopStart)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.LightGray.copy(alpha = 0.9f))
                                    .padding(8.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Request More Photos",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                photoDialog.value = true
                                            },
                                        textAlign = TextAlign.End
                                    )
                                    if (photoDialog.value) {
                                        AlertDialog(
                                            onDismissRequest = { photoDialog.value = false },
                                            title = { Text("Request Submitted") },
                                            text = {
                                                Text("Your request to get more photos has been submitted to the owner. You will be notified in the notification tab once the owner uploads the images.")
                                            },
                                            confirmButton = {
                                                Button(onClick = {
                                                    photoDialog.value = false
                                                    val ownerNotificationId = FirebaseDatabase.getInstance().reference.push().key ?: ""
                                                    val ownerNotification = Notification(
                                                        title = "More Photos Requested",
                                                        message = "A user has requested more photos for the product: $productName.",
                                                        recipientEmail = it.ownerEmail,
                                                        requesterEmail = currentUserEmail!!,
                                                        timestamp = System.currentTimeMillis(),
                                                        showActions = false, // No actions required for the owner
                                                        notificationId = ownerNotificationId,
                                                        type = "upload_request"
                                                    )
                                                    FirebaseDatabase.getInstance().reference.child("notifications").child(ownerNotificationId).setValue(ownerNotification)
                                                }) {
                                                    Text("OK")
                                                }
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = "Message Owner",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                navController.navigate("chat_screen/${it.ownerEmail ?: ""}/${currentUserEmail ?: ""}")
                                            },
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                    text = "Get Owner Details",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showDialog.value = true
                                        },
                                    textAlign = TextAlign.End
                                )
                                if (showDialog.value) {
                                    AlertDialog(
                                        onDismissRequest = { showDialog.value = false },
                                        title = { Text("Request Details") },
                                        text = {
                                            OutlinedTextField(
                                                value = purposeText.value,
                                                onValueChange = { purposeText.value = it },
                                                label = { Text("Enter your purpose to request the details") }
                                            )
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    if (it.ownerEmail.isNotEmpty()) {
                                                        val notificationId =
                                                            FirebaseDatabase.getInstance().reference.push().key
                                                                ?: ""
                                                        val notificationMessage =
                                                            "Purpose: ${purposeText.value}\nRequester Email: ${user.email}\nRequest details for $productName. Approve or reject?"
                                                        val notification = Notification(
                                                            title = "Owner Details Request",
                                                            message = notificationMessage,
                                                            recipientEmail = it.ownerEmail,
                                                            requesterEmail = currentUserEmail!!,
                                                            timestamp = System.currentTimeMillis(),
                                                            showActions = true,
                                                            notificationId = notificationId,
                                                            approverEmail = it.ownerEmail
                                                        )
                                                        val dbRef =
                                                            FirebaseDatabase.getInstance().reference.child(
                                                                "notifications"
                                                            )
                                                        dbRef.child(notificationId)
                                                            .setValue(notification)
                                                        showDialog.value = false
                                                        showConfirmationDialog.value = true
                                                    }
                                                }
                                            ) {
                                                Text("Submit")
                                            }
                                        },
                                        dismissButton = {
                                            Button(onClick = { showDialog.value = false }) {
                                                Text("Cancel")
                                            }
                                        }
                                    )
                                }
                                if (showConfirmationDialog.value) {
                                    AlertDialog(
                                        onDismissRequest = { showConfirmationDialog.value = false },
                                        title = { Text("Request Submitted") },
                                        text = {
                                            Text("Your request has been submitted to the owner. You will be notified in the notification tab once the owner approves your request.")
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    showConfirmationDialog.value = false
                                                    val loggedInUserEmail =
                                                        FirebaseAuth.getInstance().currentUser?.email
                                                    if (loggedInUserEmail != null) {
                                                        val notification = Notification(
                                                            title = "Request Submitted",
                                                            message = "Your request to get owner details for $productName has been submitted.",
                                                            recipientEmail = loggedInUserEmail,
                                                            timestamp = System.currentTimeMillis(),
                                                            showActions = false,
                                                            approverEmail = it.ownerEmail
                                                        )
                                                        val dbRef =
                                                            FirebaseDatabase.getInstance().reference.child(
                                                                "notifications"
                                                            )
                                                        dbRef.push().setValue(notification)
                                                    } else {
                                                    }
                                                }
                                            ) {
                                                Text("OK")
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
            }
        } ?: run {
            Text("Product not found")
        }
    }

fun fetchProductDetailFromFirebase(productName: String, callback: (Product?, DatabaseError?) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("products").orderByChild("productName").equalTo(productName)

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val product = snapshot.children.firstOrNull()?.getValue(Product::class.java)
            callback(product, null)
        }

        override fun onCancelled(error: DatabaseError) {
            callback(null, error)
        }
    })
}
