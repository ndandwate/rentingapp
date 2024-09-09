package com.example.myrentingapplication

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myrentingapplication.ui.theme.MyRentingApplicationTheme
//homepage after splash screen
@Composable
fun homepage(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.backgroundimage),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(5.dp)
        ) {
            Text(
                text = "Welcome to",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    color = Color.Black
                ),
                modifier = Modifier.padding(top = 25.dp, bottom = 35.dp)
            )
            Box(
                modifier = Modifier
                    .background(color = Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .padding(bottom = 3.dp)
            ) {
                Text(
                    text = "RENTAL BUDDY",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 40.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(top = 1.dp, bottom = 5.dp)
                )
            }
            MovingText(
                text = "A Smart Renting & Price Compare Application",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Red
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            Image(
                painter = painterResource(id = R.drawable.rentingimage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(350.dp)
                    .clip(CircleShape)
            )
            Button(
                onClick = { navController.navigate("landingpage") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Cyan.copy(alpha = 0.4f)
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Let's Go",
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
@Composable
fun MovingText(text: String, style: TextStyle, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult = remember(text, style) { textMeasurer.measure(text, style) }

    val infiniteTransition = rememberInfiniteTransition()
    val density = LocalDensity.current

    val textWidthPx = with(density) { textLayoutResult.size.width.toFloat() }

    val offsetX by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val parentWidthPx = with(density) { maxWidth.toPx() }
        val maxOffset = (parentWidthPx - textWidthPx) / 2

        val animatedOffset = offsetX.dp

        Text(
            text = text,
            style = style,
            maxLines = 1,
            modifier = Modifier.offset(x = animatedOffset)
        )
    }
}
@Preview(showBackground = true)
@Composable
fun homepagePreview() {
    MyRentingApplicationTheme {
        val navController = rememberNavController()
        homepage(navController)
    }
}
