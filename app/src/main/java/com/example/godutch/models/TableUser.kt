package com.example.godutch.models

import org.json.JSONArray

data class TableUser (
    var id : String,
    var username : String,
    var orders : JSONArray,
    var isPaid : Boolean
)