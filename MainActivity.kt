package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileCard()
        }
    }
}

@Composable
fun ProfileCard() {
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1247) }

    val buttonColor by animateColorAsState(
        targetValue = if (isFollowing) Color(0xFF4CAF50) else Color(0xFF2196F3),
        animationSpec = tween(durationMillis = 300),
        label = "buttonColor"
    )

    val profileBackgroundColor by animateColorAsState(
        targetValue = if (isFollowing) Color(0xFF4CAF50) else Color(0xFF2196F3),
        animationSpec = tween(durationMillis = 300),
        label = "profileBackgroundColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(profileBackgroundColor, CircleShape)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile Avatar",
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ramazan A.",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Android learner",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = "3rd year student at SDU university",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$followerCount followers",
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = profileBackgroundColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isFollowing = !isFollowing
                followerCount = if (isFollowing) followerCount + 1 else followerCount - 1
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFollowing) "Unfollow" else "Follow",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileCard() {
    ProfileCard()
}
