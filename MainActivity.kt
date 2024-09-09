package com.example.myrentingapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myrentingapplication.ui.theme.MyRentingApplicationTheme
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult


class MainActivity : ComponentActivity() {
    private lateinit var paymentSheet: PaymentSheet
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Initialization
        Firebase.auth
        Firebase.database

        PaymentConfiguration.init(
            applicationContext,
            "pk_live_51PrSrZP5cbwvieClf0DB5Qgxv8glZ1fxhGO1UWaFXrrY3qxESOmDjAC8ZqPGAvapZODm7I8HDsT5V1dv1Xma2LV100ZQEw6oRT"  // Replace with your actual publishable key
        )

        // Payment Sheet Initialization
        paymentSheet = PaymentSheet(
            this,
            ::onPaymentSheetResult
        )

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val loggedInUser = User(
            name = currentUser?.displayName ?: "Unknown",
            email = currentUser?.email ?: "",
            phone = currentUser?.phoneNumber ?: "",
            address = ""
        )

        setContent {
            MyRentingApplicationTheme {
                val navController = rememberNavController()
                NavHostScreen(navController, loggedInUser)
            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
            }
            is PaymentSheetResult.Failed -> {
            }
            is PaymentSheetResult.Canceled -> {
            }
        }
    }
}

//defining navigations
@Composable
fun NavHostScreen(navController: NavHostController,loggedInUser: User) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("homepage") { homepage(navController) }
        composable("landingpage") { LandingPage(navController) }
        composable("renteelogin") { RenteeLogin(navController) }
        composable("renterlogin") { RenterLogin(navController) }
        composable("renteesignup") { RenteeSignup(navController) }
        composable("rentersignup") { RenterSignup(navController) }
        composable("upload_screen") { UploadScreen(navController) }
       composable("profile_screen") { ProfileScreen(navController) }
        composable("notificationscreen") { NotificationScreen(navController) }
        composable("profile_scren") { ProfileScren(navController) }
        composable(
            route = "ownerDetails/{productName}/{approverEmail}",
            arguments = listOf(
                navArgument("productName") { type = NavType.StringType },
                navArgument("approverEmail") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            val approverEmail = backStackEntry.arguments?.getString("approverEmail") ?: ""
            OwnerDetails(navController, productName, approverEmail)
        }

        composable("logout_rentee") { RenteeLogin(navController) }
        composable("photography/{message}/{requesterEmail}") { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message") ?: ""
            val requesterEmail = backStackEntry.arguments?.getString("requesterEmail") ?: ""
            Photographs(message = message, requesterEmail = requesterEmail, navController = navController)
        }
        composable("viewphotos") { viewPhotos(navController) }
        composable("logout_renter") { RenterLogin(navController)}
        composable(
            "rent/{pricePerDay}/{minimalRentalPeriod}/{ownerEmail}/{productName}",
            arguments = listOf(
                navArgument("pricePerDay") { type = NavType.IntType }, // IntType for pricePerDay
                navArgument("minimalRentalPeriod") { type = NavType.StringType },
                navArgument("ownerEmail") { type = androidx.navigation.NavType.StringType },
                navArgument("productName") { type = NavType.StringType }

            )
        ) { backStackEntry ->
            val pricePerDay = backStackEntry.arguments?.getInt("pricePerDay") // Retrieve pricePerDay as Int
            val minimalRentalPeriod = backStackEntry.arguments?.getString("minimalRentalPeriod")
            val ownerEmail = backStackEntry.arguments?.getString("ownerEmail")
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            Rent(navController, pricePerDay, minimalRentalPeriod,ownerEmail,productName)
        }
        composable("search_screen") { SearchScreen(navController)}
        composable(
            "rentproceed/{pricePerDay}/{minimalRentalPeriod}/{ownerEmail}/{duration}/{totalPrice}/{startDate}/{endDate}/{productName}",
            arguments = listOf(
                navArgument("pricePerDay") { type = NavType.IntType },
                navArgument("minimalRentalPeriod") { type = NavType.StringType },
                navArgument("ownerEmail") { type = NavType.StringType },
                navArgument("duration") { type = NavType.IntType },
                navArgument("totalPrice") { type = NavType.IntType },
                navArgument("startDate") { type = NavType.StringType },
                navArgument("endDate") { type = NavType.StringType },
                navArgument("productName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val pricePerDay = backStackEntry.arguments?.getInt("pricePerDay")
            val minimalRentalPeriod = backStackEntry.arguments?.getString("minimalRentalPeriod")
            val ownerEmail = backStackEntry.arguments?.getString("ownerEmail")
            val duration = backStackEntry.arguments?.getInt("duration") ?: 0
            val totalPrice = backStackEntry.arguments?.getInt("totalPrice") ?: 0
            val startDate = backStackEntry.arguments?.getString("startDate")
            val endDate = backStackEntry.arguments?.getString("endDate")
            val productName = backStackEntry.arguments?.getString("productName") ?: ""

            RentProceed(
                navController = navController,
                pricePerDay = pricePerDay,
                minimalRentalPeriod = minimalRentalPeriod,
                ownerEmail = ownerEmail,
                duration = duration,
                totalPrice = totalPrice,
                startDate = startDate,
                endDate = endDate,
                productName = productName
            )
        }
        composable("electronicsScreen") { ElectronicsCategory(navController) }
        composable("furnitureScreen") { FurnitureCategory(navController) }
        composable("photographyScreen") { PhotographyCategory(navController) }
        composable("booksScreen") { BooksCategory(navController) }
        composable("dailyAppliancesScreen") { DailyAppliancesCategory(navController) }
        composable(
            route = "chat_screen/{ownerEmail}/{userEmail}",
            arguments = listOf(
                navArgument("ownerEmail") { type = NavType.StringType },
                navArgument("userEmail") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val ownerEmail = backStackEntry.arguments?.getString("ownerEmail")
            val userEmail = backStackEntry.arguments?.getString("userEmail")
            ChatScreen(navController, ownerEmail = ownerEmail, userEmail = userEmail)
        }
        composable("product_detail/{productName}") { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("productName") ?: ""
            electronicsProductDetail(productName, navController, loggedInUser)
        }

    }
}
