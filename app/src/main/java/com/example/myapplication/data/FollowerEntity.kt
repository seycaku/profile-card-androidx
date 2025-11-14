package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followers")
data class FollowerEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val username: String,
    val isFollowingBack: Boolean = false,
    val avatarColor: Int = 0xFF2196F3.toInt()
)

