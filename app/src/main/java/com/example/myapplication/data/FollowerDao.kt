package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowerDao {
    @Query("SELECT * FROM followers ORDER BY id")
    fun getAllFollowers(): Flow<List<FollowerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollower(follower: FollowerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowers(followers: List<FollowerEntity>)
    
    @Update
    suspend fun updateFollower(follower: FollowerEntity)
    
    @Delete
    suspend fun deleteFollower(follower: FollowerEntity)
    
    @Query("DELETE FROM followers")
    suspend fun deleteAllFollowers()
}

