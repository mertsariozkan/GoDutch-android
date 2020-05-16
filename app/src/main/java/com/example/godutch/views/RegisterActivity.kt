package com.example.godutch.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.godutch.Payload.Responses.SignupResponse

import com.example.godutch.models.User
import kotlinx.android.synthetic.main.activity_register.*

import com.example.godutch.R
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val activity: Activity = this
        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)

        val cityDropdown: Spinner = findViewById(R.id.cityDropdown)
        ArrayAdapter.createFromResource(
            this,
            R.array.cities_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            cityDropdown.adapter = adapter
        }

        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            var user = User(emailField.text.toString())
            user.password = passwordField.text.toString()
            user.username = usernameField.text.toString()
            user.roles = Array(1,init = {"user"})
            var passwordRepeat = repeatPasswordField.text.toString()

            if(user.email != "" && user.username != "" && user.password != "" && passwordRepeat != "" && user.password == passwordRepeat) {
                val url = AppCommons.RootUrl + "auth/signup"
                var result : SignupResponse? = null
                request.POST(url, user, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                val json = JSONObject(responseData)
                                val id = json["id"]
                                println("Request Successful!!")
                                result = SignupResponse(
                                    json["id"] as String,
                                    json["username"] as String,
                                    json["email"] as String,
                                    json["roles"] as JSONArray,
                                    json["accessToken"] as String,
                                    json["tokenType"] as String
                                )
                                val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
                                val editor = preferences.edit()
                                editor.putString("token", result!!.tokenType+" "+ result!!.accessToken)
                                editor.putString("userId", result!!.id)
                                editor.putString("username", result!!.username)
                                editor.putString("email", result!!.email)
                                editor.apply()

                                val intent = Intent(activity, RestaurantsActivity::class.java)
                                startActivity(intent)
                                finish()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Can not sign up")
                    }
                })
            }


        }

    }

}
