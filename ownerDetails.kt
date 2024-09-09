package com.example.myrentingapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
//screen to fetch the owner details and display them to user
@Composable
fun OwnerDetails(navController: NavController, productName: String?, ownerEmail: String?) {
    val product = remember { mutableStateOf<Product?>(null) }
    val dbRef = FirebaseDatabase.getInstance().reference.child("products")
    LaunchedEffect(ownerEmail) {
        if (ownerEmail != null) {
            dbRef.orderByChild("ownerEmail").equalTo(ownerEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val foundProduct = child.getValue(Product::class.java)
                                product.value = foundProduct
                                break
                            }
                        } else {
                            product.value = null
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                }
                )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.productbackground),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Gray.copy(alpha = 0.5f), shape = CircleShape)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart)
                .padding(8.dp)
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (product.value != null) {
                val prod = product.value!!
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Owner Details",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .padding(bottom = 32.dp)
                    )
                    Text(
                        text = "Owner Name: ${prod.ownerName}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Owner Email: ${prod.ownerEmail}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Owner Phone: + ${prod.ownerNumber}",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Owner Address: ${prod.ownerAddress}",
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "No product found for the given owner.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
