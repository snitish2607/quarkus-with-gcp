package org.nirp.dto

data class User (
    val username : String,
    val password : String,
    val roles: List<String>
)