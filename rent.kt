package com.example.myrentingapplication

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.ZoneId
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
//screen for user to select the dates for renting a product
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Rent(navController: NavController, pricePerDay: Int?, minimalRentalPeriod: String?, ownerEmail: String?,productName: String?) {
    println("Minimal Rental Period received: $minimalRentalPeriod")
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { navController.popBackStack() }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select Duration",
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
            CalendarView(minimalRentalPeriod, pricePerDay,navController,ownerEmail, productName)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    minimalRentalPeriod: String?,
    pricePerDay: Int?,
    navController: NavController,
    ownerEmail: String?,
    productName: String?
) {
    val today = LocalDate.now()
    val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    var showDialog by remember { mutableStateOf(false) }
    var bookedDates by remember { mutableStateOf<List<LocalDate>>(emptyList()) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = todayMillis)
    LaunchedEffect(productName) {
        val fetchedDates = fetchBookedDates(productName)
        bookedDates = fetchedDates
    }
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Text(
                    text = if (startDate == null) "Set Start Date" else "Set End Date",
                    modifier = Modifier
                        .clickable {
                            try {
                                if (startDate == null) {
                                    startDate = datePickerState.selectedDateMillis
                                } else {
                                    endDate = datePickerState.selectedDateMillis
                                    showDialog = false
                                }
                            } catch (e: Exception) {
                                Log.e("CalendarView", "Error during date selection", e)
                            }
                        }
                        .padding(16.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .clickable { showDialog = false }
                        .padding(16.dp)
                )
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { selectedDate ->
                    selectedDate >= todayMillis && !bookedDates.contains(
                        LocalDate.ofEpochDay(selectedDate / (24 * 60 * 60 * 1000))
                    )
                }
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Select Dates")
            }
            Button(
                onClick = {
                    startDate = null
                    endDate = null
                }
            ) {
                Text("Reset Dates")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val selectedStartDate = startDate?.let { millis ->
            LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
        }
        val selectedEndDate = endDate?.let { millis ->
            LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
        }
        val isDateRangeValid = selectedStartDate != null && selectedEndDate != null &&
                !selectedEndDate.isBefore(selectedStartDate)
        val numberOfDays = if (isDateRangeValid) {
            (selectedEndDate!!.toEpochDay() - selectedStartDate!!.toEpochDay() + 1).toInt()
        } else {
            0
        }
        val minimalRentalPeriodInt = minimalRentalPeriod?.split(" ")?.firstOrNull()?.toIntOrNull() ?: 0
        val totalPrice = (pricePerDay ?: 0) * numberOfDays
        Text(
            text = buildAnnotatedString {
                when {
                    selectedStartDate != null && selectedEndDate != null && minimalRentalPeriodInt > 0 && numberOfDays < minimalRentalPeriodInt -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Selected Range: ")
                        }
                        append("$selectedStartDate - $selectedEndDate\n\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Duration: ")
                        }
                        append("$numberOfDays days\n\n")
                        append("Minimum rental period is $minimalRentalPeriod days. Please select a longer duration.\n\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Total Price: ")
                        }
                        append("$totalPrice")
                    }
                    selectedStartDate != null && selectedEndDate != null -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Selected Range: ")
                        }
                        append("$selectedStartDate - $selectedEndDate\n\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Duration: ")
                        }
                        append("$numberOfDays days\n\n")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Total Price: £")
                        }
                        append("$totalPrice")
                    }
                    selectedStartDate != null -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Selected Start Date: ")
                        }
                        append("$selectedStartDate")
                    }
                    else -> append("No date selected")
                }
            },
            color = if (selectedStartDate != null && selectedEndDate != null && minimalRentalPeriodInt > 0 && numberOfDays < minimalRentalPeriodInt) Color.Red else Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (selectedStartDate != null && selectedEndDate != null) {
                    val numberOfDays = (selectedEndDate.toEpochDay() - selectedStartDate.toEpochDay() + 1).toInt()
                    val minimalRentalPeriodInt = minimalRentalPeriod?.split(" ")?.firstOrNull()?.toIntOrNull() ?: 0
                    val totalPrice = (pricePerDay ?: 0) * numberOfDays
                    val formattedStartDate = selectedStartDate.toString()
                    val formattedEndDate = selectedEndDate.toString()
                    val safeProductName = productName ?: "Unknown"
                    navController.navigate(
                        "rentproceed/${pricePerDay ?: 0}/${minimalRentalPeriod ?: "Unknown"}/" +
                                "${ownerEmail ?: "Unknown"}/${numberOfDays}/${totalPrice}/" +
                                "${formattedStartDate}/${formattedEndDate}/${safeProductName}"
                    )
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = selectedStartDate != null && selectedEndDate != null &&
                    numberOfDays >= (minimalRentalPeriod?.split(" ")?.firstOrNull()?.toIntOrNull() ?: 0)
        ) {
            Text(text = "Next")
        }
    }
}
fun LocalDate.rangeTo(endInclusive: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var currentDate = this
    while (!currentDate.isAfter(endInclusive)) {
        dates.add(currentDate)
        currentDate = currentDate.plusDays(1)
    }
    return dates
}
suspend fun fetchBookedDates(productName: String?): List<LocalDate> {
    if (productName.isNullOrEmpty()) return emptyList()

    val database = Firebase.database.reference.child("booked_dates").child(productName)
    val bookedDates = mutableListOf<LocalDate>()
    return suspendCoroutine { continuation ->
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { dateSnapshot ->
                    val startDateStr = dateSnapshot.child("startDate").getValue(String::class.java)
                    val endDateStr = dateSnapshot.child("endDate").getValue(String::class.java)

                    if (startDateStr != null && endDateStr != null) {
                        val startDate = LocalDate.parse(startDateStr)
                        val endDate = LocalDate.parse(endDateStr)

                        bookedDates.addAll(startDate.rangeTo(endDate))
                    }
                }
                continuation.resume(bookedDates)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("CalendarView", "Error fetching booked dates: ${error.message}")
                continuation.resume(emptyList())
            }
        })
    }
}



