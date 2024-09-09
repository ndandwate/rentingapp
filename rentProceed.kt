package com.example.myrentingapplication

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.stripe.android.Stripe
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.compose.material3.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.delay

//Final Payment screen
@Composable
fun RentProceed(
    navController: NavController,
    pricePerDay: Int?,
    minimalRentalPeriod: String?,
    ownerEmail: String?,
    duration: Int,
    totalPrice: Int,
    startDate: String?,
    endDate: String?,
    productName: String?
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var deliveryAddress by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var isDeliveryAddressValid by remember { mutableStateOf(false) }
    var cvc by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val additionalAmount = 6
    val finalPrice = totalPrice + additionalAmount
    var isProcessing by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    if (activity == null) {
        Log.e("RentProceed", "Activity is null")
        return
    }
    val stripe = remember { Stripe(activity, "pk_live_51PrSrZP5cbwvieClf0DB5Qgxv8glZ1fxhGO1UWaFXrrY3qxESOmDjAC8ZqPGAvapZODm7I8HDsT5V1dv1Xma2LV100ZQEw6oRT") }
    var paymentFormVisible by remember { mutableStateOf(false) }
    Log.d("RentProceed", "Component initialized")
    fun processPayment(clientSecret: String) {
        val paymentMethodCreateParams = PaymentMethodCreateParams.create(
            PaymentMethodCreateParams.Card.Builder()
                .setNumber(cardNumber)
                .setCvc(cvc)
                .setExpiryMonth(expiryMonth.toIntOrNull() ?: 0)
                .setExpiryYear(expiryYear.toIntOrNull() ?: 0)
                .build()
        )

        val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
            paymentMethodCreateParams,
            clientSecret
        )

        stripe.confirmPayment(activity, confirmParams)
    }
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(5000)

            if (cardNumber == "4659432614210939") {
                successMessage = "Payment of £$finalPrice has been successful! Your order is placed for dates $startDate to $endDate."
                saveBooking(
                    context = context,
                    productName = product?.productName ?: "",
                    ownerEmail = ownerEmail,
                    startDate = startDate ?: "",
                    endDate = endDate ?: ""
                )
                saveBooking(
                    context = context,
                    productName = product?.productName ?: "",
                    ownerEmail = ownerEmail,
                    startDate = startDate ?: "",
                    endDate = endDate ?: ""
                )
                saveBookedDatesToFirebase(
                    productName = product?.productName ?: "",
                    startDate = startDate ?: "",
                    endDate = endDate ?: ""
                )
                sendNotificationToUserAndOwner(
                    ownerEmail = ownerEmail ?: "",
                    requesterEmail = FirebaseAuth.getInstance().currentUser?.email ?: "",
                    productName = product?.productName ?: "",
                    startDate = startDate ?: "",
                    endDate = endDate ?: "",
                    totalPrice = finalPrice
                )
            } else {
                successMessage = "Payment failed. Please check your card details."
            }
            isProcessing = false
        }
    }
    if (successMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { successMessage = "" },
            title = { Text(text = if (successMessage.contains("successful")) "Payment Successful" else "Payment Failed") },
            text = { Text(text = successMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        if (successMessage.contains("successful")) {
                            confirmBooking(
                                context = context,
                                productName = product?.productName ?: "",
                                ownerEmail = ownerEmail,
                                startDate = startDate ?: "",
                                endDate = endDate ?: ""
                            )
                            navController.navigate("search_screen")
                        }
                        successMessage = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.productbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        LaunchedEffect(ownerEmail) {
            Log.d("RentProceed", "LaunchedEffect triggered with ownerEmail: $ownerEmail")
            if (ownerEmail != null) {
                try {
                    product = withContext(Dispatchers.IO) {
                        getProductByOwnerEmail(ownerEmail)
                    }
                    if (product == null) {
                        errorMessage = "No product found for this email."
                        Log.d("RentProceed", "No product found for ownerEmail: $ownerEmail")
                    }
                } catch (e: Exception) {
                    errorMessage = "Error fetching data: ${e.message}"
                    Log.e("RentProceed", "Error fetching data: ${e.message}")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        Log.d("RentProceed", "Back button clicked")
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "Review and Pay",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.width(48.dp))

                Button(
                    onClick = {
                        paymentFormVisible = !paymentFormVisible
                        Log.d("RentProceed", "Rent button clicked, paymentFormVisible: $paymentFormVisible")
                    },
                    enabled = isDeliveryAddressValid
                ) {
                    Text("Rent")
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                item {
                    errorMessage?.let {
                        Text("Error: $it", color = Color.Red)
                    } ?: run {
                        product?.let {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    val imageData = parseProductImageData(it.productImage)
                                    when (imageData) {
                                        is ImageData.Base64Image -> {
                                            val bitmap =
                                                imageData.base64String.decodeBase64ToBitmap()
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = "Product Image",
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .padding(end = 16.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text("Invalid Base64 Image", color = Color.Red)
                                            }
                                        }

                                        is ImageData.UrlImage -> {
                                            val painter =
                                                rememberAsyncImagePainter(model = imageData.url)
                                            Image(
                                                painter = painter,
                                                contentDescription = "Product Image",
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .padding(end = 16.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                    Text(
                                        text = it.productName,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Dates",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))

                                    val startFormatted = formatDate(startDate)
                                    val endFormatted = formatDate(endDate)

                                    Text(
                                        text = "$startFormatted to $endFormatted",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Owner Address",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it.ownerAddress ?: "No address provided",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Price Details",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        ),
                                        color = Color.Black,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "£${pricePerDay ?: 0} x $duration days",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 15.sp
                                            ),
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "£${totalPrice}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp
                                            ),
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Postage Charges",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 15.sp
                                            ),
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "£6",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp
                                            ),
                                            color = Color.Black
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 15.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )

                                        Text(
                                            text = "£${finalPrice}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 16.sp
                                            ),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Delivery Address",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Please provide the address for delivery and if any special instructions for delivery",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 18.sp
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TextField(
                                value = deliveryAddress,
                                onValueChange = { address ->
                                    deliveryAddress = address
                                    isDeliveryAddressValid = address.isNotBlank()
                                    Log.d("RentProceed", "Delivery address updated: $address, isValid: $isDeliveryAddressValid")
                                },
                                placeholder = { Text("Enter your delivery address") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .height(120.dp),
                                maxLines = 5,
                                singleLine = false,
                                isError = !isDeliveryAddressValid
                            )
                            if (!isDeliveryAddressValid) {
                                Text(
                                    text = "Delivery address is required",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Payment Details",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Please provide the card details to proceed for payment",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    fontSize = 18.sp
                                ),
                                color = Color.Black,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (paymentFormVisible) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(modifier = Modifier.padding(16.dp)) {
                                    TextField(
                                        value = cardNumber,
                                        onValueChange = {
                                            if (it.length <= 16) {
                                                cardNumber = it
                                            }
                                        },
                                        label = { Text("Card Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = cardNumber.length != 16 && cardNumber.isNotEmpty()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = expiryMonth,
                                        onValueChange = {
                                            if (it.toIntOrNull() in 1..12 || it.isEmpty()) {
                                                expiryMonth = it
                                            }
                                        },
                                        label = { Text("Expiry Month (MM)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = (expiryMonth.toIntOrNull() !in 1..12 && expiryMonth.isNotEmpty())
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = expiryYear,
                                        onValueChange = {
                                            if (it.length <= 2) {
                                                expiryYear = it
                                            }
                                        },
                                        label = { Text("Expiry Year (YY)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = expiryYear.length != 2 && expiryYear.isNotEmpty()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    TextField(
                                        value = cvc,
                                        onValueChange = {
                                            if (it.length <= 3) {
                                                cvc = it
                                            }
                                        },
                                        label = { Text("CVC") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = cvc.length != 3 && cvc.isNotEmpty()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            val clientSecret = when (finalPrice) {
                                                7 -> "pi_3Px7oMP5cbwvieCl0wf5e4qG_secret_hyKaX9S4VAtpCGERCAh7Ht5jJ"
                                                8 -> "pi_3Pud7zP5cbwvieCl1xM1WrLC_secret_NpYu9GhaTTWAEakAxzuqpOQKs"
                                                9 -> "pi_3Pud8FP5cbwvieCl1KVIscRw_secret_c4bv2bDLAnoGO551FV13GitwH"
                                                10 -> "pi_3Pud8kP5cbwvieCl1j3vca7h_secret_qYJLlSt4Kr8OlOHaiOdR1rKrz"
                                                11 -> "pi_3Pud99P5cbwvieCl1yyBozGo_secret_qvMgR6k9cvE8ggfvAlTR5tK4E"
                                                12 -> "pi_3PutiuP5cbwvieCl15G6wGFk_secret_jhjU5PPhmp4brfxkSPfXMMaMV5"
                                                13 -> "pi_3Pud9qP5cbwvieCl1ERkz5gA_secret_3uYpuCaLRy4IDTOXJlTvnFUap"
                                                14 -> "pi_3PudADP5cbwvieCl1jDhvx3m_secret_TwinCTl3pU3CGkZrJq25y3Pbn"
                                                15 -> "pi_3PudATP5cbwvieCl03U1Rb5E_secret_r20AlCh1ve3vMvmi147AbB898"
                                                16 -> "pi_3PudAkP5cbwvieCl1pT9Fid6_secret_BtmaC2tuhkVnrhufgKpOwiIHZ"
                                                17 -> "pi_3PudB1P5cbwvieCl1RAuXhJC_secret_fEmrW6D90xFrq8XHMqVxwkzXW"
                                                18 -> "pi_3PudBPP5cbwvieCl0lg2Vf9p_secret_on3DmJmC3TZM9xr1jUek4jlPb"
                                                19 -> "pi_3PudBfP5cbwvieCl1Wtac1WO_secret_LSwZFq9QomlTvWFMsVx1YGn5k"
                                                20 -> "pi_3PudCTP5cbwvieCl1hElDMyv_secret_oa97NinntPztnFZQ4I728FgPL"
                                                21 -> "pi_3PudCmP5cbwvieCl1YXJnq0O_secret_Ou9c4UOShB6mDa9STTyVh1ppP"
                                                else -> "pi_3Px7ouP5cbwvieCl09cgH4N3_secret_5nXsHaqdzOX8YmLH6TNe9qprD"
                                            }
                                            isProcessing = true
                                            processPayment(clientSecret)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = cardNumber.length == 16 &&
                                                expiryMonth.toIntOrNull() in 1..12 &&
                                                expiryYear.length == 2 &&
                                                cvc.length == 3 && !isProcessing
                                    ) {
                                        Text(text = "Pay Now")
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
fun saveBookedDatesToFirebase(productName: String, startDate: String, endDate: String) {
    val database = FirebaseDatabase.getInstance().reference
    val bookedDatesRef = database.child("booked_dates").child(productName)
    val bookingKey = bookedDatesRef.push().key
    val bookingDetails = mapOf(
        "startDate" to startDate,
        "endDate" to endDate
    )
    bookingKey?.let {
        bookedDatesRef.child(it).setValue(bookingDetails)
    }
}
suspend fun sendNotificationToUserAndOwner(
    ownerEmail: String,
    requesterEmail: String,
    productName: String,
    startDate: String,
    endDate: String,
    totalPrice: Int
) {
    try {
        val dbRef = FirebaseDatabase.getInstance().reference.child("notifications")
        val notificationForOwnerId = dbRef.push().key ?: ""
        val notificationForUserId = dbRef.push().key ?: ""
        val notificationForOwner = Notification(
            title = "New Order Placed",
            message = "An order has been placed for your product \"$productName\". Dates: $startDate to $endDate. Total Price: £$totalPrice.",
            timestamp = System.currentTimeMillis(),
            recipientEmail = ownerEmail,
            requesterEmail = requesterEmail,
            showActions = false,
            notificationId = notificationForOwnerId,
            status = "pending",
            approverEmail = ownerEmail,
            type = "order"
        )
        val notificationForUser = Notification(
            title = "Order Confirmed",
            message = "Your order for \"$productName\" has been placed successfully for dates $startDate to $endDate. Total Price: £$totalPrice.",
            timestamp = System.currentTimeMillis(),
            recipientEmail = requesterEmail,
            requesterEmail = requesterEmail,
            showActions = false,
            notificationId = notificationForUserId,
            status = "confirmed",
            approverEmail = ownerEmail,
            type = "order"
        )
        dbRef.child(notificationForOwnerId).setValue(notificationForOwner).await()
        dbRef.child(notificationForUserId).setValue(notificationForUser).await()

    } catch (e: Exception) {
        Log.e("Notifications", "Failed to send notifications", e)
    }
}
fun formatDate(dateString: String?): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        val date = LocalDate.parse(dateString)
        date.format(formatter)
    } catch (e: Exception) {
        "Invalid Date"
    }
}

fun parseProductImageData(imageData: String): ImageData {
    return if (isBase64Image(imageData)) {
        ImageData.Base64Image(imageData.substringAfter(","))
    } else {
        ImageData.UrlImage(imageData)
    }
}

fun isBase64Image(data: String): Boolean {
    return data.startsWith("data:image") || isValidBase64(data)
}

fun isValidBase64(data: String): Boolean {
    return try {
        Base64.decode(data, Base64.DEFAULT)
        true
    } catch (e: IllegalArgumentException) {
        false
    }
}
fun String.decodeBase64ToBitmap(): ImageBitmap? {
    return try {
        val decodedBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        Log.e("decodeBase64ToBitmap", "Error decoding Base64 string", e)
        null
    }
}

suspend fun getProductByOwnerEmail(ownerEmail: String): Product? {
    Log.d("getProductByOwnerEmail", "Fetching product for ownerEmail: $ownerEmail")
    return try {
        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("products")
        val snapshot = productsRef.get().await()
        val product = snapshot.children
            .mapNotNull { it.getValue(Product::class.java) }
            .firstOrNull { it.ownerEmail == ownerEmail }
        if (product != null) {
            Log.d("getProductByOwnerEmail", "Product found: $product")
        } else {
            Log.d("getProductByOwnerEmail", "No product found for ownerEmail: $ownerEmail")
        }
        product
    } catch (e: Exception) {
        Log.e("getProductByOwnerEmail", "Error fetching product", e)
        null
    }
}
fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("bookings_prefs", Context.MODE_PRIVATE)
}

fun saveBooking(context: Context, productName: String, ownerEmail: String?, startDate: String, endDate: String) {
    val sharedPreferences = getSharedPreferences(context)
    val editor = sharedPreferences.edit()
    val key = "${productName}_${ownerEmail ?: "unknown"}"
    val bookingInfo = "$startDate,$endDate"
    editor.putString(key, bookingInfo)
    editor.apply()
}
fun confirmBooking(context: Context, productName: String, ownerEmail: String?, startDate: String, endDate: String) {
    println("Booking confirmed for product: $productName, owner: $ownerEmail, from $startDate to $endDate")
}


