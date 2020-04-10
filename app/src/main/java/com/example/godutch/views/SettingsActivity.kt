package com.example.godutch.views

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.godutch.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        val logoutLayout = findViewById<ConstraintLayout>(R.id.logoutLayout)
        logoutLayout.setOnClickListener {
            val builder = AlertDialog.Builder(this)

            builder.setTitle("Çıkış")
            builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?")

            builder.setPositiveButton("YES") { dialog, which ->

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
    }
}
