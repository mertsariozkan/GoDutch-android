package com.example.godutch.Payload.Requests

data class SignInRequest(var username: String) {
    lateinit var password: String
}