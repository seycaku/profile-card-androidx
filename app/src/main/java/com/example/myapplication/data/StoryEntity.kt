package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: Int,
    val userName: String,
    val isViewed: Boolean = false,
    val avatarColor: Int = 0xFF9C27B0.toInt()
)

