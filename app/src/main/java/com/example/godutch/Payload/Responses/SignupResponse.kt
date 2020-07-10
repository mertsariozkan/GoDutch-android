package com.example.godutch.Payload.Responses

import org.json.JSONArray

data class SignupResponse(
    var id: String,
    var name: String,
    var surname: String,
    var email: String,
    var roles: JSONArray,
    var accessToken: String,
    var tokenType: String

)