package com.example.godutch.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.godutch.Payload.Requests.DeleteUserRequest
import com.example.godutch.R
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_table.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


class TableActivity : AppCompatActivity(), MenuFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {

    }

    private val menuFragment = MenuFragment()
    private var args = Bundle()
    var restaurantId: String? = null
    var tableName: String? = null
    var token: String? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_menu -> {
                changeFragment(menuFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_table -> {
                val tableFragment = TableFragment()
                tableFragment.arguments = args
                changeFragment(tableFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_myorders -> {
                val ordersFragment = OrdersFragment()
                ordersFragment.arguments = args
                changeFragment(ordersFragment)
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
        supportActionBar?.setHomeAsUpIndicator(R.mipmap.exit)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        restaurantId = intent.getStringExtra("restaurantId")
        var restaurantName = intent.getStringExtra("restaurantName")
        tableName = intent.getStringExtra("tableName")
        var userName = intent.getStringExtra("userName")

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        token = preferences.getString("token", "")
        var userId = preferences.getString("userId", "")

        args.putString("restaurantId", restaurantId)
        args.putString("restaurantName", restaurantName)
        args.putString("tableName", tableName)
        args.putString("token", token)
        args.putString("userId", userId)
        args.putString("userName", userName)

        menuFragment.arguments = args

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        changeFragment(menuFragment)
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(com.example.godutch.R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val client: OkHttpClient = OkHttpClient()
        val request: OkHttpRequest = OkHttpRequest(client)

        var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName

        AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Are you sure you want to leave table?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(
                android.R.string.yes
            ) { dialog, whichButton ->
                request.DELETE(
                    url,
                    DeleteUserRequest(args["userId"] as String, args["userName"] as String),
                    token!!,
                    object : Callback {
                        override fun onResponse(call: Call?, response: Response) {
                            runOnUiThread {
                                try {
                                    val intent = Intent(this@TableActivity, RestaurantsActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            println("Can not get table orders.")
                        }
                    })
            }
            .setNegativeButton(android.R.string.no, null).show()
        return true
    }

}
