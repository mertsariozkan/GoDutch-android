package com.example.godutch.utils

import com.google.gson.Gson
import okhttp3.*

class OkHttpRequest(var client : OkHttpClient) {

    fun GET(url: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun POST(url: String, parameters: Any, callback: Callback): Call {
        val jsonString : String = if(parameters is String) parameters else Gson().toJson(parameters)

        val requestBody = RequestBody.create(JSON, jsonString)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()


        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    //Request with token

    fun GET(url: String, token: String, callback: Callback): Call {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token)
            .build()

        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    fun POST(url: String, parameters: Any, token: String, callback: Callback): Call {

        val jsonString : String = if(parameters is String) parameters else Gson().toJson(parameters)

        val requestBody = RequestBody.create(JSON, jsonString)

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", token)
            .post(requestBody)
            .build()


        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    companion object {
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }
}