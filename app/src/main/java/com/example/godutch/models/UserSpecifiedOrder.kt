package com.example.godutch.models

data class UserSpecifiedOrder (
    var userId : String,
    var item : MenuItem,
    var count : Int
)