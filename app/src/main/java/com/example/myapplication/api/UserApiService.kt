package com.example.myapplication.api

import retrofit2.http.GET

interface UserApiService {
    @GET("users")
    suspend fun getUsers(): List<ApiUser>
}

