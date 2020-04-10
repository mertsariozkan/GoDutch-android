package com.example.godutch.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.godutch.Payload.Requests.SignInRequest
import com.example.godutch.Payload.Responses.SignupResponse
import com.example.godutch.R
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)
        if (token != null) {
            val intent = Intent(this, RestaurantsActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_login)


        val activity: Activity = this
        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val usernameField: EditText = findViewById(R.id.usernameField)
        val passwordField: EditText = findViewById(R.id.passwordField)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            var signInRequest = SignInRequest(usernameField.text.toString())
            signInRequest.password = passwordField.text.toString()

            if(signInRequest.username != "" && signInRequest.password != "") {
                val url = AppCommons.RootUrl + "auth/signin"
                var result : SignupResponse? = null
                request.POST(url, signInRequest, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        activity.runOnUiThread {
                            try {
                                val json = JSONObject(responseData)
                                println("Request Successful!!")
                                result = SignupResponse(
                                    json["id"] as String,
                                    json["username"] as String,
                                    json["email"] as String,
                                    json["roles"] as JSONArray,
                                    json["accessToken"] as String,
                                    json["tokenType"] as String
                                )
                                val preferences = activity.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
                                val editor = preferences.edit()
                                editor.putString("token", result!!.tokenType+" "+ result!!.accessToken)
                                editor.apply()

                                val intent = Intent(activity, RestaurantsActivity::class.java)
                                startActivity(intent)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        activity.runOnUiThread {
                            Toast.makeText(applicationContext, "Can't log in", Toast.LENGTH_SHORT).show()
                        }

                    }
                })
            }

        }

        val newAccountButton = findViewById<TextView>(R.id.newAccountButton)
        newAccountButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }


}