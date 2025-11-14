package com.example.myapplication

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.ApiUser
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf

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

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userDao()
    private val followerDao = database.followerDao()
    private val storyDao = database.storyDao()
    private val apiService = RetrofitClient.userApiService

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val userFlow: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    var name by mutableStateOf("Ramazan A.")
    var bio by mutableStateOf("Android learner")
    var additionalInfo by mutableStateOf("3rd year student at SDU university")
    var followerCount by mutableStateOf(1247)
    var isFollowing by mutableStateOf(false)

    val followersFlow: StateFlow<List<Follower>> = followerDao.getAllFollowers()
        .map { entities ->
            entities.map { entity ->
                Follower(
                    id = entity.id,
                    name = entity.name,
                    username = entity.username,
                    isFollowingBack = entity.isFollowingBack,
                    avatarColor = Color(entity.avatarColor)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val storiesFlow: StateFlow<List<Story>> = storyDao.getAllStories()
        .map { entities ->
            entities.map { entity ->
                Story(
                    id = entity.id,
                    userName = entity.userName,
                    isViewed = entity.isViewed,
                    avatarColor = Color(entity.avatarColor)
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            userFlow.collect { user ->
                if (user != null) {
                    name = user.name
                    bio = user.bio.ifEmpty { "Android learner" }
                    additionalInfo = user.additionalInfo.ifEmpty { "3rd year student at SDU university" }
                    followerCount = user.followerCount
                    isFollowing = user.isFollowing
                } else {
                    initializeDefaultUser()
                }
            }
        }
    }

    private suspend fun initializeDefaultUser() {
        val defaultUser = UserEntity(
            id = 1,
            name = "Ramazan A.",
            username = "ramazan",
            email = "ramazan@example.com",
            bio = "Android learner",
            additionalInfo = "3rd year student at SDU university",
            followerCount = 1247,
            isFollowing = false
        )
        userDao.insertUser(defaultUser)

        val defaultStories = listOf(
            StoryEntity(1, "Alex", false, 0xFFE91E63.toInt()),
            StoryEntity(2, "Maria", true, 0xFF9C27B0.toInt()),
            StoryEntity(3, "John", false, 0xFF2196F3.toInt()),
            StoryEntity(4, "Sarah", false, 0xFF4CAF50.toInt()),
            StoryEntity(5, "Mike", true, 0xFFFF9800.toInt()),
            StoryEntity(6, "Emma", false, 0xFFF44336.toInt())
        )
        storyDao.insertStories(defaultStories)
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val apiUsers = apiService.getUsers()
                
                if (apiUsers.isNotEmpty()) {
                    val existingFollowers = followersFlow.value
                    val existingFollowStatusMap = existingFollowers.associateBy { it.id }

                    val followerEntities = apiUsers.drop(1).take(10).mapIndexed { index, apiUser ->
                        val colors = listOf(
                            0xFFE91E63, 0xFF9C27B0, 0xFF2196F3, 0xFF4CAF50,
                            0xFFFF9800, 0xFFF44336, 0xFF00BCD4, 0xFF8BC34A,
                            0xFFFFEB3B, 0xFF795548
                        )
                        val existingFollower = existingFollowStatusMap[apiUser.id]
                        FollowerEntity(
                            id = apiUser.id,
                            name = apiUser.name,
                            username = "@${apiUser.username}",
                            isFollowingBack = existingFollower?.isFollowingBack ?: (index % 3 == 0),
                            avatarColor = existingFollower?.avatarColor?.value?.toInt() ?: colors[index % colors.size].toInt()
                        )
                    }
                    followerDao.deleteAllFollowers()
                    followerDao.insertFollowers(followerEntities)

                    val storyEntities = apiUsers.take(6).mapIndexed { index, apiUser ->
                        val colors = listOf(
                            0xFFE91E63, 0xFF9C27B0, 0xFF2196F3,
                            0xFF4CAF50, 0xFFFF9800, 0xFFF44336
                        )
                        StoryEntity(
                            id = apiUser.id,
                            userName = apiUser.username,
                            isViewed = index % 2 == 0,
                            avatarColor = colors[index % colors.size].toInt()
                        )
                    }
                    storyDao.deleteAllStories()
                    storyDao.insertStories(storyEntities)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(newName: String, newBio: String, newAdditionalInfo: String) {
        viewModelScope.launch {
            val currentUser = userFlow.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    name = newName,
                    bio = newBio,
                    additionalInfo = newAdditionalInfo
                )
                userDao.updateUser(updatedUser)
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val currentUser = userFlow.value
            if (currentUser != null) {
                val newFollowingState = !currentUser.isFollowing
                val newFollowerCount = if (newFollowingState) {
                    currentUser.followerCount + 1
                } else {
                    currentUser.followerCount - 1
                }
                val updatedUser = currentUser.copy(
                    isFollowing = newFollowingState,
                    followerCount = newFollowerCount
                )
                userDao.updateUser(updatedUser)
            }
        }
    }

    fun markStoryAsViewed(storyId: Int) {
        viewModelScope.launch {
            val stories = storiesFlow.value
            val story = stories.find { it.id == storyId }
            if (story != null) {
                val storyEntity = StoryEntity(
                    id = story.id,
                    userName = story.userName,
                    isViewed = true,
                    avatarColor = story.avatarColor.value.toInt()
                )
                storyDao.updateStory(storyEntity)
            }
        }
    }

    fun toggleFollowerStatus(followerId: Int) {
        viewModelScope.launch {
            val followers = followersFlow.value
            val follower = followers.find { it.id == followerId }
            if (follower != null) {
                val followerEntity = FollowerEntity(
                    id = follower.id,
                    name = follower.name,
                    username = follower.username,
                    isFollowingBack = !follower.isFollowingBack,
                    avatarColor = follower.avatarColor.value.toInt()
                )
                followerDao.updateFollower(followerEntity)
            }
        }
    }

    fun removeFollower(followerId: Int) {
        viewModelScope.launch {
            val followers = followersFlow.value
            val follower = followers.find { it.id == followerId }
            if (follower != null) {
                val followerEntity = FollowerEntity(
                    id = follower.id,
                    name = follower.name,
                    username = follower.username,
                    isFollowingBack = follower.isFollowingBack,
                    avatarColor = follower.avatarColor.value.toInt()
                )
                followerDao.deleteFollower(followerEntity)
            }
        }
    }

    fun restoreFollower(follower: Follower) {
        viewModelScope.launch {
            val followerEntity = FollowerEntity(
                id = follower.id,
                name = follower.name,
                username = follower.username,
                isFollowingBack = follower.isFollowingBack,
                avatarColor = follower.avatarColor.value.toInt()
            )
            followerDao.insertFollower(followerEntity)
        }
    }
}

