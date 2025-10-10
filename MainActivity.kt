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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        ProfileCard(
            modifier = Modifier.padding(innerPadding),
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1247) }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
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
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Android learner",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "3rd year student at SDU university",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$followerCount followers",
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    color = profileBackgroundColor,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isFollowing) {
                            showUnfollowDialog = true
                        } else {
                            isFollowing = true
                            followerCount += 1
                            scope.launch {
                                snackbarHostState.showSnackbar("You are now following Ramazan A.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(
                        text = if (isFollowing) "Unfollow" else "Follow",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isFollowing = false
                        followerCount -= 1
                        showUnfollowDialog = false
                    }
                ) {
                    Text("Unfollow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnfollowDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Unfollow User") },
            text = { Text("Are you sure you want to unfollow Ramazan A.?") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}
