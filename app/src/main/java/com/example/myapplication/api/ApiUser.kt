package com.example.myapplication.api

data class ApiUser(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val address: Address? = null,
    val phone: String? = null,
    val website: String? = null,
    val company: Company? = null
)

data class Address(
    val street: String,
    val suite: String,
    val city: String,
    val zipcode: String,
    val geo: Geo? = null
)

data class Geo(
    val lat: String,
    val lng: String
)

data class Company(
    val name: String,
    val catchPhrase: String? = null,
    val bs: String? = null
)

