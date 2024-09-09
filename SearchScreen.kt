package com.example.myrentingapplication

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
//Function to display all the categories to user
data class Category(
    val name: String,
    val imageResId: Int,
    val route: String
)
data class Notification(
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val recipientEmail: String = "",
    val requesterEmail: String = "",
    val showActions: Boolean = false,
    val notificationId: String = "",
    val status: String = "",
    val approverEmail: String = "",
    val type: String = "",
    val actionLabel: String? = null,
)
@Composable
fun LogoAnimationScreen(onAnimationFinished: () -> Unit) {
    val startAnimation = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation.value = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 5000
        )
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation.value) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 8000
        )
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splashwallpaper),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Splash Screen Image",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .scale(scale)
                .alpha(alpha)
        )
    }
    LaunchedEffect(Unit) {
        delay(7000)
    }
}
@Composable
fun SearchScreen(navController: NavController) {
    var showLogoAnimation by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    val auth: FirebaseAuth = Firebase.auth
    val database: FirebaseDatabase = Firebase.database
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(auth.currentUser) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(User::class.java)
                    user = userData
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
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
        var searchText by remember { mutableStateOf("") }
        val categories = listOf(
            Category("Electronics", R.drawable.electronics, "electronicsScreen"),
            Category("Photography", R.drawable.camera, "photographyScreen"),
            Category("Books", R.drawable.books, "booksScreen"),
            Category("Daily Appliances", R.drawable.homeappliances, "dailyAppliancesScreen"),
            Category("Furniture", R.drawable.furniture, "furnitureScreen")
        )
        Scaffold(
            bottomBar = {
                BottomNavigation(navController, selectedIndex = 0)
            }
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
                ) {
                    if (user != null) {
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
                                    .size(60.dp)
                                    .clickable {
                                        auth.signOut()
                                        navController.navigate("renteelogin") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(1f)
                        ) {
                            Text(
                                text = "Welcome, ${user!!.name}",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Red.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color.LightGray)
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = {
                                Text(
                                    text = "Search by category",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            singleLine = true,
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val category = categories.find { it.name.equals(searchText, ignoreCase = true) }
                                    category?.let {
                                        navController.navigate(it.route)
                                    }
                                }
                            ),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        items(categories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = category.name,
                                        style = TextStyle(
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.LightGray)
                                            .clickable {
                                                navController.navigate(category.route)
                                            }
                                            .width(180.dp)
                                            .height(220.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = category.imageResId),
                                            contentDescription = category.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }


                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(190.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScren(navController: NavController) {
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
            BottomNavigation(navController, selectedIndex = 2)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                            navController.navigate("renteelogin") {
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
fun BottomNavigation(navController: NavController, selectedIndex: Int) {
    val items = listOf(
        BottomNav("Search", R.drawable.search),
        BottomNav("Profile", R.drawable.profile)
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
                        "Search" -> navController.navigate("search_screen") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        "Profile" -> navController.navigate("profile_scren") {
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

data class BottomNav(val label: String, val icon: Int)

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController = navController)
}
