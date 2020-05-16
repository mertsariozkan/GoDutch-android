package com.example.godutch.views

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.godutch.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val username = preferences.getString("username", "")
        val email = preferences.getString("email", "")

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        val emailField = findViewById<TextView>(R.id.emailField)
        emailField.text = email
        val userNameField = findViewById<TextView>(R.id.usernameField)
        userNameField.text = username

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Çıkış")
            builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?")

            builder.setPositiveButton("YES") { dialog, which ->
                preferences.edit().clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            builder.setNegativeButton("NO") { dialog, which ->
                dialog.dismiss()
            }

            val alert = builder.create()
            alert.show()

        }

        val creditCardsButton = findViewById<Button>(R.id.creditCardsButton)
        creditCardsButton.setOnClickListener {
            val intent = Intent(this, CreditCardListActivity::class.java)
            startActivity(intent)
        }


    }
}
