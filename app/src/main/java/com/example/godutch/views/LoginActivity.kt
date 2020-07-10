package com.example.godutch.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
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

    var progressBar : ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", null)
        val restaurantId = preferences.getString("restaurantId", null)
        if (token != null) {
            if(restaurantId == null) {
                val intent = Intent(this, RestaurantsActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, TableListActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        setContentView(R.layout.activity_login)


        val activity: Activity = this
        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        progressBar = findViewById(R.id.login_progressbar)
        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)

        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            startProgressBar()
            val username = emailField.text.toString().split("@")[0]
            var signInRequest = SignInRequest(username)
            signInRequest.password = passwordField.text.toString()

            if(signInRequest.username != "" && signInRequest.password != "") {
                val url = AppCommons.RootUrl + "auth/signin"
                var result : SignupResponse? = null
                request.POST(url, signInRequest, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        activity.runOnUiThread {
                            try {
                                stopProgressBar()
                                val json = JSONObject(responseData)
                                println("Request Successful!!")
                                result = SignupResponse(
                                    json["id"] as String,
                                    json["name"] as String,
                                    json["surname"] as String,
                                    json["email"] as String,
                                    json["roles"] as JSONArray,
                                    json["accessToken"] as String,
                                    json["tokenType"] as String
                                )
                                val editor = preferences.edit()
                                editor.putString("token", result!!.tokenType+" "+ result!!.accessToken)
                                editor.putString("userId", result!!.id)
                                editor.putString("username", result!!.name + " " + result!!.surname)
                                editor.putString("email", result!!.email)

                                if(result!!.roles[0] == "ROLE_WAITER")
                                    editor.putString("restaurantId", json["restaurantId"] as String)

                                editor.apply()

                                if(result!!.roles[0] == "ROLE_USER") {
                                    val intent = Intent(activity, RestaurantsActivity::class.java)
                                    startActivity(intent)
                                }
                                else if(result!!.roles[0] == "ROLE_WAITER") {
                                    val intent = Intent(activity, TableListActivity::class.java)
                                    startActivity(intent)
                                }
                                finish()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                activity.runOnUiThread {
                                    stopProgressBar()
                                    Toast.makeText(applicationContext, "Username or password is not correct.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        activity.runOnUiThread {
                            stopProgressBar()
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

    private fun startProgressBar() {
        progressBar!!.visibility = ProgressBar.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun stopProgressBar() {
        progressBar!!.visibility = ProgressBar.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


}
