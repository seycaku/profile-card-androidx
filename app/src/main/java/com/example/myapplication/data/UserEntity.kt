package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val bio: String = "",
    val additionalInfo: String = "",
    val followerCount: Int = 0,
    val isFollowing: Boolean = false
)

