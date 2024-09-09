package com.example.myrentingapplication

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
//screen for owner to upload the images
@Composable
fun Photographs(message: String, requesterEmail: String, navController: NavController) {
    val productContent = message.substringAfter("product:", "").trim()
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(true) }
    val storage = FirebaseStorage.getInstance()
    val database = FirebaseDatabase.getInstance()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        selectedImageUris = uris
    }
    LaunchedEffect(Unit) {
        deleteDatabaseBranch(productContent, database) {
            deleteStorageFiles(productContent, storage) {
                isDeleting = false
            }
        }
    }
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.productbackground),
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Product Name: $productContent",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text(text = "Select Images from Gallery")
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                if (selectedImageUris.isNotEmpty()) {
                    Button(
                        onClick = {
                            isUploading = true
                            uploadMultipleImagesToFirebase(
                                uris = selectedImageUris,
                                productContent = productContent,
                                storage = storage,
                                database = database,
                                requesterEmail = requesterEmail
                            ) { urls ->
                                uploadedImageUrls = urls
                                isUploading = false
                            }
                        },
                        enabled = !isUploading && !isDeleting,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = when {
                            isDeleting -> "Preparing..."
                            isUploading -> "Uploading..."
                            else -> "Upload Images"
                        })
                    }
                }
                if (uploadedImageUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Image(s) Uploaded Successfully!!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun deleteDatabaseBranch(
    productContent: String,
    database: FirebaseDatabase,
    onComplete: () -> Unit
) {
    val dbRef = database.reference.child("images_upload")
    dbRef.removeValue().addOnSuccessListener {
        onComplete()
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
        onComplete()
    }
}
private fun deleteStorageFiles(
    productContent: String,
    storage: FirebaseStorage,
    onComplete: () -> Unit
) {
    val storageRef = storage.reference.child("images/$productContent")

    storageRef.listAll().addOnSuccessListener { listResult ->
        val deletionTasks = listResult.items.map { item ->
            item.delete()
        }
        Tasks.whenAllComplete(deletionTasks).addOnCompleteListener {
            onComplete()
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            onComplete()
        }
    }.addOnFailureListener { exception ->
        exception.printStackTrace()
        onComplete()
    }
}

private fun uploadMultipleImagesToFirebase(
    uris: List<Uri>,
    productContent: String,
    storage: FirebaseStorage,
    database: FirebaseDatabase,
    requesterEmail: String,
    onSuccess: (List<String>) -> Unit
) {
    val uploadedUrls = mutableListOf<String>()
    uris.forEachIndexed { index, uri ->
        val storageRef = storage.reference.child("images/$productContent/${System.currentTimeMillis()}_$index.jpg")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val dbRef = database.reference.child("images_upload").push()
                dbRef.setValue(downloadUri.toString())
                uploadedUrls.add(downloadUri.toString())
                if (uploadedUrls.size == uris.size) {
                    onSuccess(uploadedUrls)
                    notifyRequesterOfUpload(requesterEmail, productContent, database)
                }
            } else {
            }
        }
    }
}

private fun notifyRequesterOfUpload(
    requesterEmail: String,
    productContent: String,
    database: FirebaseDatabase
) {
    val requesterNotificationId = database.reference.push().key ?: ""
    val notification = Notification(
        title = "Images Uploaded!",
        message = "The images you requested for the product: '$productContent' have been uploaded successfully. You can now view them.",
        recipientEmail = requesterEmail,
        timestamp = System.currentTimeMillis(),
        showActions = false,
        notificationId = requesterNotificationId,
        actionLabel = "View Photos",
        type = "VIEW_PHOTOS"
    )
    database.reference.child("notifications").child(requesterNotificationId).setValue(notification)
}