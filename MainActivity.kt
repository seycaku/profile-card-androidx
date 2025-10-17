package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class Follower(
    val id: Int,
    val name: String,
    val username: String,
    val isFollowingBack: Boolean = false,
    val avatarColor: Color = Color(0xFF2196F3)
)

data class Story(
    val id: Int,
    val userName: String,
    val isViewed: Boolean = false,
    val avatarColor: Color = Color(0xFF9C27B0)
)

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
        ProfileContent(
            modifier = Modifier.padding(innerPadding),
            snackbarHostState = snackbarHostState
        )
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    var isFollowing by rememberSaveable { mutableStateOf(false) }
    var followerCount by rememberSaveable { mutableStateOf(1247) }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val initialStories = listOf(
        Story(1, "Alex", false, Color(0xFFE91E63)),
        Story(2, "Maria", true, Color(0xFF9C27B0)),
        Story(3, "John", false, Color(0xFF2196F3)),
        Story(4, "Sarah", false, Color(0xFF4CAF50)),
        Story(5, "Mike", true, Color(0xFFFF9800)),
        Story(6, "Emma", false, Color(0xFFF44336))
    )

    val initialFollowers = listOf(
        Follower(1, "Alex Johnson", "@alexj", false, Color(0xFFE91E63)),
        Follower(2, "Maria Garcia", "@mariag", true, Color(0xFF9C27B0)),
        Follower(3, "John Smith", "@johns", false, Color(0xFF2196F3)),
        Follower(4, "Sarah Williams", "@sarahw", true, Color(0xFF4CAF50)),
        Follower(5, "Mike Brown", "@mikeb", false, Color(0xFFFF9800)),
        Follower(6, "Emma Davis", "@emmad", false, Color(0xFFF44336)),
        Follower(7, "Chris Wilson", "@chrisw", true, Color(0xFF00BCD4)),
        Follower(8, "Lisa Anderson", "@lisaa", false, Color(0xFF8BC34A)),
        Follower(9, "Tom Martinez", "@tomm", false, Color(0xFFFFEB3B)),
        Follower(10, "Nina Taylor", "@ninat", true, Color(0xFF795548))
    )

    var stories by remember { mutableStateOf(initialStories) }
    var followers by remember { mutableStateOf(initialFollowers) }
    var removedFollower by remember { mutableStateOf<Follower?>(null) }

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

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            StoriesSection(
                stories = stories,
                onStoryClick = { story ->
                    stories = stories.map {
                        if (it.id == story.id) it.copy(isViewed = true) else it
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar("Viewing ${story.userName}'s story")
                    }
                }
            )
        }

        item {
            ProfileCard(
                isFollowing = isFollowing,
                followerCount = followerCount,
                buttonColor = buttonColor,
                profileBackgroundColor = profileBackgroundColor,
                onFollowClick = {
                    if (isFollowing) {
                        showUnfollowDialog = true
                    } else {
                        isFollowing = true
                        followerCount += 1
                        scope.launch {
                            snackbarHostState.showSnackbar("You are now following Ramazan A.")
                        }
                    }
                }
            )
        }

        item {
            Text(
                text = "Followers",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        items(
            items = followers,
            key = { it.id }
        ) { follower ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissValue ->
                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                        removedFollower = follower
                        followers = followers.filter { it.id != follower.id }

                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "${follower.name} removed",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                removedFollower?.let { removed ->
                                    followers = (followers + removed).sortedBy { it.id }
                                }
                                removedFollower = null
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE53935))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Remove",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                content = {
                    FollowerItem(
                        follower = follower,
                        onFollowClick = {
                            followers = followers.map {
                                if (it.id == follower.id) {
                                    it.copy(isFollowingBack = !it.isFollowingBack)
                                } else it
                            }
                            scope.launch {
                                val message = if (!follower.isFollowingBack) {
                                    "You are now following ${follower.name}"
                                } else {
                                    "Unfollowed ${follower.name}"
                                }
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                },
                enableDismissFromStartToEnd = false
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun StoriesSection(
    stories: List<Story>,
    onStoryClick: (Story) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = "Stories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(stories) { story ->
                    StoryItem(
                        story = story,
                        onClick = { onStoryClick(story) }
                    )
                }
            }
        }
    }
}

@Composable
fun StoryItem(
    story: Story,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = 3.dp,
                    color = if (story.isViewed) Color.Gray else story.avatarColor,
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(story.avatarColor.copy(alpha = 0.3f))
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Story avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = story.avatarColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = story.userName,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (story.isViewed) Color.Gray else Color.Black
        )
    }
}

@Composable
fun ProfileCard(
    isFollowing: Boolean,
    followerCount: Int,
    buttonColor: Color,
    profileBackgroundColor: Color,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
                onClick = onFollowClick,
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

@Composable
fun FollowerItem(
    follower: Follower,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(follower.avatarColor, CircleShape)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Follower avatar",
                    modifier = Modifier
                        .size(30.dp)
                        .align(Alignment.Center),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = follower.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = follower.username,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (follower.isFollowingBack) Color(0xFF4CAF50) else Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (follower.isFollowingBack) "Following" else "Follow",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}
