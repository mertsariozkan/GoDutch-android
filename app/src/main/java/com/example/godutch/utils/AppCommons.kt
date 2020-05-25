package com.example.godutch.utils

import com.example.godutch.models.Restaurant
import okhttp3.MediaType

object AppCommons {
    val JSON = MediaType.parse("application/json; charset=utf-8")
    const val RootUrl = "https://frozen-forest-81678.herokuapp.com/api/"
}