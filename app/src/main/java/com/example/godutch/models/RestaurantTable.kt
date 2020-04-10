package com.example.godutch.models

import org.json.JSONArray
import org.json.JSONObject

data class RestaurantTable(
    var name : String,
    var isProtected : Boolean,
    var passCode : String,
    var users : JSONArray
)