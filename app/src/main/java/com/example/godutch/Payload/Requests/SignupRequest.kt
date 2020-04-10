package com.example.godutch.Payload.Requests

data class SignupRequest(var email: String) {
    lateinit var username: String
    lateinit var city: String
    lateinit var password: String
    lateinit var role: String

}