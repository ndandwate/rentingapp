package com.example.myrentingapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
//screen to let user choose between Rentee and Renter
@Composable
fun LandingPage(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.landingbackground),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().graphicsLayer(alpha = 0.5f)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(top = 200.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.chooserole),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 30.dp)
                    .size(190.dp)
                    .clip(CircleShape)
                    .graphicsLayer(
                        scaleX = 1.3f,
                        scaleY = 1.3f,
                        alpha = 0.8f,
                        translationY = 35f
                    )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
                    .graphicsLayer(translationY = -80f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rentee),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(150.dp)
                        .clip(CircleShape)
                        .graphicsLayer(
                            scaleX = 2.3f,
                            scaleY = 2.3f,
                            alpha = 0.9f,
                            //translationY = 50f
                        ).clickable {
                            navController.navigate("renteelogin")
                        }
                )
                Image(
                    painter = painterResource(id = R.drawable.renter),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(150.dp)
                        .clip(CircleShape)
                        .graphicsLayer(
                            scaleX = 1.8f,
                            scaleY = 1.8f,
                            alpha = 0.9f,
                            translationY = 25f
                        )
                        .clickable {
                            navController.navigate("renterlogin")
                        }
                )
            }
        }
    }
}
