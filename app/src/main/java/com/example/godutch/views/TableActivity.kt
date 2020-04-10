package com.example.godutch.views

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import com.example.godutch.R
import kotlinx.android.synthetic.main.activity_table.*

class TableActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_menu -> {
                message.setText(R.string.title_menu)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_table -> {
                message.setText(R.string.title_table)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myorders -> {
                message.setText(R.string.title_myorders)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table)

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
