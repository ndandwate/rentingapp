package com.example.myrentingapplication

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

//screen to upload products
data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = ""
)

@Composable
fun UploadScreen(navController: NavController) {
    // State Variables
    var ownerName by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var ownerNumber by remember { mutableStateOf("") }
    var ownerAddress by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var productType by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var minimalRentalPeriod by remember { mutableStateOf("") }
    var pricePerDay by remember { mutableStateOf(0) }
    var productDescription by remember { mutableStateOf("") }
    var productImage by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadResult by remember { mutableStateOf("") }
    var currentScreen by remember { mutableStateOf("owner_info") }
    var showLogoAnimation by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database
    LaunchedEffect(auth.currentUser) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    isLoading = false
                }
                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                    Log.e("Firebase", "Error fetching user data: ${error.message}")
                }
            })
        } else {
            isLoading = false
        }
    }
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(3000)
            showLogoAnimation = false
        }
    }
    if (showLogoAnimation) {
        LogoAnimationScreen(onAnimationFinished = { showLogoAnimation = false })
    } else {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController, selectedIndex = 0) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backgroundlandingpage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate("notificationscreen")
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.logout),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    auth.signOut()
                                    navController.navigate("renterlogin") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                        )
                    }
                    when (currentScreen) {
                        "owner_info" -> {
                            OwnerInfoScreen(
                                ownerName = ownerName,
                                onOwnerNameChange = { ownerName = it },
                                ownerEmail = ownerEmail,
                                onOwnerEmailChange = { ownerEmail = it },
                                ownerNumber = ownerNumber,
                                onOwnerNumberChange = { ownerNumber = it },
                                ownerAddress = ownerAddress,
                                onOwnerAddressChange = { ownerAddress = it },
                                onNext = { currentScreen = "product_details" }
                            )
                        }
                        "product_details" -> {
                            ProductDetailsScreen(
                                productName = productName,
                                onProductNameChange = { productName = it },
                                productType = productType,
                                onProductTypeChange = { productType = it },
                                category = category,
                                onCategoryChange = { category = it },
                                brand = brand,
                                onBrandChange = { brand = it },
                                onNext = { currentScreen = "product_info" },
                                onBack = { currentScreen = "owner_info" }
                            )
                        }
                        "product_info" -> {
                            ProductInfoScreen(
                                ownerName = ownerName,
                                ownerEmail = ownerEmail,
                                ownerNumber = ownerNumber,
                                ownerAddress = ownerAddress,
                                category = category,
                                selectedCategory = selectedCategory,
                                productName = productName,
                                productType = productType,
                                brand = brand,
                                onBrandChange = { brand = it },
                                minimalRentalPeriod = minimalRentalPeriod,
                                onMinimalRentalPeriodChange = { minimalRentalPeriod = it },
                                pricePerDay = pricePerDay,
                                onPricePerDayChange = { pricePerDay = it },
                                productDescription = productDescription,
                                onProductDescriptionChange = { productDescription = it },
                                productImage = productImage,
                                onProductImageChange = { productImage = it },
                                imageUri = imageUri,
                                onImageUriChange = { imageUri = it },
                                uploadResult = uploadResult,
                                onUploadResultChange = { uploadResult = it },
                                onBack = { currentScreen = "product_details" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerInfoScreen(
    ownerName: String,
    onOwnerNameChange: (String) -> Unit,
    ownerEmail: String,
    onOwnerEmailChange: (String) -> Unit,
    ownerNumber: String,
    onOwnerNumberChange: (String) -> Unit,
    ownerAddress: String,
    onOwnerAddressChange: (String) -> Unit,
        onNext: () -> Unit
) {
    var rawNumber by remember { mutableStateOf(ownerNumber.removePrefix("+44-")) }
    val displayedNumber = "+44-" + rawNumber

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.listingbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "List an Item",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(25.dp))
            Text("Owner Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = ownerName,
                onValueChange = onOwnerNameChange,
                label = { Text("Owner Name *") },
                isError = ownerName.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = ownerEmail,
                onValueChange = onOwnerEmailChange,
                label = { Text("Owner Email *") },
                isError = ownerEmail.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column {
                TextField(
                    value = displayedNumber,
                    onValueChange = { newValue ->
                        val cleanValue = newValue.removePrefix("+44-").filter { it.isDigit() }
                        if (cleanValue.length <= 10) {
                            rawNumber = cleanValue
                            onOwnerNumberChange("+44-" + cleanValue)
                        }
                    },
                    label = { Text("Owner Number *") },
                    isError = rawNumber.length != 10,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (rawNumber.length != 10) {
                    Text(
                        text = "Number needs to be exactly 10 digits.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = ownerAddress,
                onValueChange = onOwnerAddressChange,
                label = { Text("Owner Address *") },
                isError = ownerAddress.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = onNext,
                enabled = ownerName.isNotEmpty() &&
                        ownerEmail.isNotEmpty() &&
                        rawNumber.length == 10 &&
                        ownerAddress.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun ProductDetailsScreen(
    productName: String,
    onProductNameChange: (String) -> Unit,
    productType: String,
    onProductTypeChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(category) }
    val categories = listOf("Electronics", "Books", "Photography", "Daily Appliances", "Furniture")
    var showDialog by remember { mutableStateOf(false) }
    var getDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.listingbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Text("Product Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(15.dp))
            Button(onClick = { showCategoryDialog = true }) {
                Text(text = "Select Category")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = selectedCategory,
                onValueChange = {  },
                label = { Text("Category *") },
                isError = selectedCategory.isEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true }
                    .padding(vertical = 8.dp),
                readOnly = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = brand,
                onValueChange = onBrandChange,
                label = { Text("Brand *") },
                isError = brand.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = productName,
                onValueChange = onProductNameChange,
                label = { Text("Product Name *") },
                isError = productName.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { getDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Exclamation Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            if (getDialog) {
                AlertDialog(
                    onDismissRequest = { getDialog = false },
                    confirmButton = {
                        TextButton(onClick = { getDialog = false }) {
                            Text("Got it")
                        }
                    },
                    title = {
                        Text(text = "How to choose a Product Name")
                    },
                    text = {
                        Column {
                            Text("A good Product name should be:\n" +"Descriptive\n" +"Easy to pronounce\n" + "Memorable\n")
                            Spacer(modifier = Modifier.height(8.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Example: Lenovo - 100S-14IBR 14 Laptop")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextField(
                value = productType,
                onValueChange = onProductTypeChange,
                label = { Text("Product Type *") },
                isError = productType.isEmpty(),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Exclamation Icon",
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Got it")
                        }
                    },
                    title = {
                        Text(text = "How to choose a Product Type")
                    },
                    text = {
                        Column {
                            Text("A good Product Type should specify the subcategory:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Example: Television,\n" +
                                    "Camera,\n" +
                                    "iPad,\n" +
                                    "Horror Book\"\n")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Back")
                }
                Button(
                    onClick = onNext,
                    enabled = selectedCategory.isNotEmpty() &&
                            brand.isNotEmpty() &&
                            productName.isNotEmpty() &&
                            productType.isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text("Next")
                }
            }

        }
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryDialog = false },
                title = { Text(text = "Select Category") },
                text = {
                    LazyColumn {
                        items(categories) { categoryItem ->
                            Text(
                                text = categoryItem,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = categoryItem
                                        onCategoryChange(categoryItem)
                                        showCategoryDialog = false
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCategoryDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
@Composable
fun ProductInfoScreen(
    ownerName: String,
    ownerEmail: String,
    ownerNumber: String,
    ownerAddress: String,
    category: String,
    selectedCategory: String,
    productName: String,
    productType: String,
    brand: String,
    onBrandChange: (String) -> Unit,
    minimalRentalPeriod: String,
    onMinimalRentalPeriodChange: (String) -> Unit,
    pricePerDay: Int,
    onPricePerDayChange: (Int) -> Unit,
    productDescription: String,
    onProductDescriptionChange: (String) -> Unit,
    productImage: String,
    onProductImageChange: (String) -> Unit,
    imageUri: Uri?,
    onImageUriChange: (Uri?) -> Unit,
    uploadResult: String,
    onUploadResultChange: (String) -> Unit,
    onBack: () -> Unit
) {
    fun addNotificationToFirebase(title: String, message: String, recipientEmail: String) {
        val notification = Notification(
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            recipientEmail = recipientEmail
        )
        val db = FirebaseDatabase.getInstance().reference.child("notifications")
        db.push().setValue(notification)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.listingbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))
            Text("Product Details", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(5.dp))

            val pickImageLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                onImageUriChange(uri)
                onProductImageChange(uri?.toString() ?: "")
                onUploadResultChange("")
            }
            fun uploadImageToFirebase(imageUri: Uri) {
                val storageRef: StorageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageRef.child("test_images/${System.currentTimeMillis()}.jpg")
                fun saveProductToFirebase(imageUrl: String, serialNumber: Long) {
                    val product = Product(
                        ownerName = ownerName,
                        ownerEmail = ownerEmail,
                        ownerNumber = ownerNumber,
                        ownerAddress = ownerAddress,
                        category = category,
                        productName = productName,
                        productType = productType,
                        brand = brand,
                        minimalRentalPeriod = minimalRentalPeriod,
                        pricePerDay = pricePerDay,
                        productDescription = productDescription,
                        productImage = imageUrl
                    )

                    val db = FirebaseDatabase.getInstance().reference
                    db.child("products")
                        .child(serialNumber.toString())
                        .setValue(product)
                        .addOnSuccessListener {
                            onUploadResultChange("Product: '$productName' uploaded successfully!")
                            val successMessage = "Product was successfully added under category '$category'. You can login as Rentee and see the product under this category."
                            addNotificationToFirebase("Upload Success", successMessage, ownerEmail)
                        }
                        .addOnFailureListener { e ->
                            onUploadResultChange("Failed to upload product: ${e.message}")
                            addNotificationToFirebase("Upload Failed", "Failed to upload your product: ${e.message}", ownerEmail)
                        }
                }

                fun getNextSerialNumberAndSave(imageUrl: String) {
                    val db = FirebaseDatabase.getInstance().reference.child("products")

                    db.orderByKey().limitToLast(1).get().addOnSuccessListener { snapshot ->
                        val lastSerialNumber = snapshot.children.lastOrNull()?.key?.toLongOrNull() ?: 0L
                        val nextSerialNumber = lastSerialNumber + 1
                        saveProductToFirebase(imageUrl, nextSerialNumber)
                    }.addOnFailureListener { e ->
                        onUploadResultChange("Failed to retrieve serial number: ${e.message}")
                        addNotificationToFirebase("Serial Number Error", "Failed to retrieve the next serial number: ${e.message}", ownerEmail)
                    }
                }

                imageRef.putFile(imageUri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            getNextSerialNumberAndSave(uri.toString())
                        }.addOnFailureListener { e ->
                            onUploadResultChange("Failed to get download URL: ${e.message}")
                            addNotificationToFirebase("URL Error", "Failed to get the download URL: ${e.message}", ownerEmail)
                        }
                    }
                    .addOnFailureListener { e ->
                        onUploadResultChange("Upload failed: ${e.message}")
                        addNotificationToFirebase("Upload Failed", "Image upload failed: ${e.message}", ownerEmail)
                    }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                var showDialog by remember { mutableStateOf(false) }
                // Input fields
                LazyColumn(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        TextField(
                            value = minimalRentalPeriod,
                            onValueChange = onMinimalRentalPeriodChange,
                            label = { Text("Minimal Rental Period") },
                            isError = minimalRentalPeriod.isEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TextField(
                            value = pricePerDay.toString(),
                            onValueChange = { newValue ->
                                onPricePerDayChange(newValue.toIntOrNull() ?: 0)
                            },
                            label = { Text("Price Per Day") },
                            isError = pricePerDay > 0,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        TextField(
                            value = productDescription,
                            onValueChange = onProductDescriptionChange,
                            label = { Text("Product Description *") },
                            isError = productDescription.isEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Exclamation Icon",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Got it")
                                    }
                                },
                                title = {
                                    Text(text = "How to write a good Description")
                                },
                                text = {
                                    Column {
                                        Text("A good product Description should include:")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("• Key features of the product")
                                        Text("• Material details")
                                        Text("• Size and color information")
                                        Text("• Any unique points")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Example: For an Laptop the description can be: \"Brand - Lenovo,\n" +
                                                "Memory - 32GB eMMC Flash Memory,\n" +
                                                "Intel Celeron - 2GB,\n" +
                                                "Color - Navy Blue,\n" +
                                                "Weight - 1.95kg\"\n")
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { pickImageLauncher.launch("image/*") }) {
                            Text("Pick Image")
                        }
                        imageUri?.let {
                            Spacer(modifier = Modifier.height(16.dp))
                            AsyncImage(
                                model = it,
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            imageUri?.let {
                                Button(
                                    onClick = { uploadImageToFirebase(it) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Submit",fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Button(
                                onClick = onBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (uploadResult.isNotBlank()) {
                            Text(
                                text = uploadResult,
                                color = if (uploadResult.contains("successfully")) Color.Green else Color.Red
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isEditing by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(auth.currentUser) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(User::class.java)
                    user = userData
                    if (userData != null) {
                        name = userData.name
                        email = userData.email
                        phone = userData.phone
                        address = userData.address
                    }
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
        }
    }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, selectedIndex = 2)
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Image(
                painter = painterResource(id = R.drawable.profilebackground),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (user != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable {
                            auth.signOut()
                            navController.navigate("renterlogin") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = "Logout",
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                if (user != null) {
                    val firstName = user!!.name.split(" ").firstOrNull() ?: "User"
                    Text(
                        text = "Welcome, $firstName",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .border(2.dp, Color.Black, CircleShape)
                            .clip(CircleShape)
                            .padding(bottom = 5.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.person),
                            contentDescription = "Person Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.userperson),
                                contentDescription = "Name Logo",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (isEditing) {
                                TextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = name,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                        Divider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mail),
                                contentDescription = "Email Logo",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = email,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                        Divider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mobile),
                                contentDescription = "Phone Logo",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (isEditing) {
                                TextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = phone,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                        Divider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.address),
                                contentDescription = "Address Logo",
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            if (isEditing) {
                                TextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    textStyle = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = address,
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                        Divider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(5.dp))
                        Button(
                            onClick = {
                                if (isEditing) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        val userRef = database.reference.child("users").child(userId)
                                        val updatedUser = User(name, email, phone, address) // Include address
                                        userRef.setValue(updatedUser)
                                        val user = auth.currentUser
                                        user?.updateEmail(email)?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                            } else {
                                            }
                                        }
                                    }
                                }
                                isEditing = !isEditing
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(text = if (isEditing) "Save" else "Edit Details")
                        }
                    }

                }
            }
        }
    }

}

@Composable
fun BottomNavigationBar(navController: NavController, selectedIndex: Int) {
    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf(
        BottomNavItem("Home", R.drawable.home),
        BottomNavItem("Profile", R.drawable.profile)
    )

    NavigationBar(
        containerColor = Color.Red.copy(alpha = 0.1f)
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedIndex == index) Color.Magenta else Color.Black
                        )
                    )
                },
                selected = selectedIndex == index,
                onClick = {
                    when (item.label) {
                        "Home" -> navController.navigate("upload_screen") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "Profile" -> navController.navigate("profile_screen") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: Int)
@Preview(showBackground = true)
@Composable
fun SearchScrenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController = navController)
}
