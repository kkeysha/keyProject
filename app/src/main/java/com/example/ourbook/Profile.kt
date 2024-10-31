package com.example.ourbook

data class Profile(
    val id: Int,
    val name: String,
    val nickname: String,
    val email: String,
    val address: String,
    val birthdate: String,
    val phone: String,
    val photo: String? // Optional untuk foto
)