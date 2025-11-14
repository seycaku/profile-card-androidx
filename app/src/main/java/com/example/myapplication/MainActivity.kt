package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                EnhancedProfileApp()
            }
        }
    }
}

@Composable
fun EnhancedProfileApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ViewModelFactory(context.applicationContext as android.app.Application)
    )

    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(
                viewModel = viewModel,
                onEditClick = { navController.navigate("editProfile") }
            )
        }
        composable("editProfile") {
            EditProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnfollowDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var removedFollower by remember { mutableStateOf<Follower?>(null) }

    val user by viewModel.userFlow.collectAsStateWithLifecycle()
    val followers by viewModel.followersFlow.collectAsStateWithLifecycle()
    val stories by viewModel.storiesFlow.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(user) {
        if (user != null) {
            viewModel.name = user!!.name
            viewModel.bio = user!!.bio.ifEmpty { "Android learner" }
            viewModel.additionalInfo = user!!.additionalInfo.ifEmpty { "3rd year student at SDU university" }
            viewModel.followerCount = user!!.followerCount
            viewModel.isFollowing = user!!.isFollowing
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val buttonColor by animateColorAsState(
        targetValue = if (viewModel.isFollowing) Color(0xFF4CAF50) else Color(0xFF2196F3),
        animationSpec = tween(durationMillis = 300),
        label = "buttonColor"
    )

    val profileBackgroundColor by animateColorAsState(
        targetValue = if (viewModel.isFollowing) Color(0xFF4CAF50) else Color(0xFF2196F3),
        animationSpec = tween(durationMillis = 300),
        label = "profileBackgroundColor"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh Data",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                ProfileCard(
                    name = viewModel.name,
                    bio = viewModel.bio,
                    additionalInfo = viewModel.additionalInfo,
                    isFollowing = viewModel.isFollowing,
                    followerCount = viewModel.followerCount,
                    buttonColor = buttonColor,
                    profileBackgroundColor = profileBackgroundColor,
                    onFollowClick = {
                        if (viewModel.isFollowing) {
                            showUnfollowDialog = true
                        } else {
                            viewModel.toggleFollow()
                            scope.launch {
                                snackbarHostState.showSnackbar("You are now following ${viewModel.name}")
                            }
                        }
                    }
                )
            }

            item {
                StoriesSection(
                    stories = stories,
                    onStoryClick = { story ->
                        viewModel.markStoryAsViewed(story.id)
                        scope.launch {
                            snackbarHostState.showSnackbar("Viewing ${story.userName}'s story")
                        }
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Followers",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${followers.size})",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            items(
                items = followers,
                key = { it.id }
            ) { follower ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            removedFollower = follower
                            viewModel.removeFollower(follower.id)

                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "${follower.name} removed",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    removedFollower?.let { removed ->
                                        viewModel.restoreFollower(removed)
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
                                viewModel.toggleFollowerStatus(follower.id)
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
    }

    if (showUnfollowDialog) {
        AlertDialog(
            onDismissRequest = { showUnfollowDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.toggleFollow()
                        showUnfollowDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Unfollowed ${viewModel.name}")
                        }
                    }
                ) {
                    Text("Unfollow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfollowDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Unfollow User") },
            text = { Text("Are you sure you want to unfollow ${viewModel.name}?") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val user by viewModel.userFlow.collectAsStateWithLifecycle()
    
    var tempName by rememberSaveable { 
        mutableStateOf(user?.name ?: viewModel.name) 
    }
    var tempBio by rememberSaveable { 
        mutableStateOf(user?.bio ?: viewModel.bio) 
    }
    var tempAdditionalInfo by rememberSaveable { 
        mutableStateOf(user?.additionalInfo ?: viewModel.additionalInfo) 
    }
    
    LaunchedEffect(user) {
        user?.let {
            tempName = it.name
            tempBio = it.bio.ifEmpty { "Android learner" }
            tempAdditionalInfo = it.additionalInfo.ifEmpty { "3rd year student at SDU university" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF2196F3), CircleShape)
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

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = tempName,
                onValueChange = { tempName = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tempBio,
                onValueChange = { tempBio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tempAdditionalInfo,
                onValueChange = { tempAdditionalInfo = it },
                label = { Text("Additional Info") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.updateProfile(tempName, tempBio, tempAdditionalInfo)
                    scope.launch {
                        snackbarHostState.showSnackbar("Profile updated successfully!")
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    text = "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Text(
                text = "Stories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
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
        modifier = Modifier
            .width(80.dp)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(
                    width = if (story.isViewed) 2.dp else 3.dp,
                    color = if (story.isViewed) Color.Gray else story.avatarColor,
                    shape = CircleShape
                )
                .padding(if (story.isViewed) 2.dp else 3.dp)
                .clip(CircleShape)
                .background(story.avatarColor.copy(alpha = 0.2f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Story avatar",
                modifier = Modifier.size(36.dp),
                tint = story.avatarColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = story.userName,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (story.isViewed) 
                MaterialTheme.colorScheme.onSurfaceVariant 
            else 
                MaterialTheme.colorScheme.onSurface,
            fontWeight = if (story.isViewed) FontWeight.Normal else FontWeight.Medium
        )
    }
}

@Composable
fun ProfileCard(
    name: String,
    bio: String,
    additionalInfo: String,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(profileBackgroundColor, CircleShape)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile Avatar",
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = bio,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (additionalInfo.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = additionalInfo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = profileBackgroundColor.copy(alpha = 0.1f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "$followerCount followers",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = profileBackgroundColor,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                ),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = if (isFollowing) "Unfollow" else "Follow",
                    fontWeight = FontWeight.Bold,
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(follower.avatarColor.copy(alpha = 0.15f), CircleShape)
                    .border(
                        width = 2.dp,
                        color = follower.avatarColor.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Follower avatar",
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.Center),
                    tint = follower.avatarColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = follower.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = follower.username,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (follower.isFollowingBack) Color(0xFF4CAF50) else Color(0xFF2196F3)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                shape = MaterialTheme.shapes.small
            ) {
                if (follower.isFollowingBack) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Following",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (follower.isFollowingBack) "Following" else "Follow",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    MyApplicationTheme {
    }
}