package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY id")
    fun getAllStories(): Flow<List<StoryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)
    
    @Update
    suspend fun updateStory(story: StoryEntity)
    
    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()
}

