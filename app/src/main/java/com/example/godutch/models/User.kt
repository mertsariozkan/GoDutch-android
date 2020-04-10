package com.example.godutch.models

data class User(var email: String) {
    lateinit var username: String
    lateinit var password: String
    lateinit var roles: Array<String>

}