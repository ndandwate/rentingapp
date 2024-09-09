package com.example.myrentingapplication

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
//splash screen
@Composable
fun SplashScreen(navController: NavHostController) {
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
        navController.navigate("homepage") {
            popUpTo("splash") { inclusive = true }
        }
    }
}
