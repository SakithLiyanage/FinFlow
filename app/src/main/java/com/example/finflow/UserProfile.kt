package com.example.finflow

import java.util.*

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val currency: String = "LKR (Sri Lankan Rupee)",
    val profileImageUrl: String = ""
)