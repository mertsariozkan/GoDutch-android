package com.example.godutch.views

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.godutch.Payload.Requests.JoinTableRequest
import com.example.godutch.Payload.Responses.GetAllTablesResponse
import com.example.godutch.Payload.Responses.GetRestaurantsResponse
import com.example.godutch.R
import com.example.godutch.models.Restaurant
import com.example.godutch.models.RestaurantTable
import com.example.godutch.models.UserConfigDto
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.RestaurantsListAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class RestaurantsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurants)

        val activity: Activity = this
        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")
        val userId = preferences.getString("userId", "")
        val username = preferences.getString("username", "")

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        val restaurantSearchField = findViewById<SearchView>(R.id.restaurantSearchField)
        val restaurantsListView = findViewById<ListView>(R.id.restaurantsListView)
        var adapter : RestaurantsListAdapter? = null
        val restaurantList = ArrayList<Restaurant>()

        var url = AppCommons.RootUrl + "restaurant/all"
        request.GET(url, token, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        println("Request Successful!!")
                        var result = GetRestaurantsResponse(
                            json["restaurants"] as JSONArray
                        )

                        for (i in 0 until result.restaurants.length()) {
                            val restaurant = result.restaurants.getJSONObject(i)
                            restaurantList.add(Restaurant(restaurant["id"] as String, restaurant["name"] as String, restaurant["latitude"] as Double, restaurant["longitude"] as Double))
                        }

                        val sortedList = restaurantList.sortedWith(compareBy { it.name })
                        adapter = RestaurantsListAdapter(activity, android.R.layout.simple_list_item_1, sortedList)
                        restaurantsListView.adapter = adapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Can not get restaurants.")
            }
        })


        restaurantsListView.setOnItemClickListener { parent, view, position, id ->
            val restaurant = adapter?.getItem(position)
            val popup = PopupMenu(this, view)

            url = AppCommons.RootUrl + "table/" + restaurant!!.id + "/all"
            request.GET(url, token, object: Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val responseData = response.body()?.string()
                    runOnUiThread {
                        try {
                            val json = JSONObject(responseData)
                            println("Request Successful!!")
                            var result = GetAllTablesResponse(
                                json["restaurantTableDtos"] as JSONArray
                            )
                            var tableList = ArrayList<JSONObject>()
                            for (i in 0 until result.restaurantTables.length()) {
                                val table = result.restaurantTables.getJSONObject(i)
                                tableList.add(table)
                            }
                            val sortedList = tableList.sortedWith(compareBy { it["name"] as String })
                            for (i in 0 until result.restaurantTables.length()) {
                                popup.menu.add(sortedList[i]["name"] as String)
                            }
                            popup.show()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    println("Can not get tables.")
                }
            })

            popup.setOnMenuItemClickListener { item ->

                url = AppCommons.RootUrl + "table/" + restaurant!!.id + "/" + item
                request.GET(url, token, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                val json = JSONObject(responseData)
                                println("Request Successful!!")
                                var table = RestaurantTable(
                                    json["name"] as String,
                                    json["protected"] as Boolean,
                                    json["passCode"] as String,
                                    json["users"] as JSONArray
                                )
                                var user = UserConfigDto(token, userId, username)
                                showPassCodeDialog(request, user, restaurant, table)

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Can not get tables.")
                    }
                })

                true
            }

        }

        restaurantSearchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                val text = newText
                /*Call filter Method Created in Custom Adapter
                    This Method Filter ListView According to Search Keyword
                 */
                adapter!!.filter.filter(text)
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.appbar_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.settingsButton) {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getContext() : Context {
        return this
    }

    private fun showPassCodeDialog(request: OkHttpRequest, user: UserConfigDto, restaurant : Restaurant, table : RestaurantTable) {
        val builder = AlertDialog.Builder(getContext())
        builder.setTitle(restaurant.name + " - " + table.name)
        if(table.isProtected)
            builder.setMessage("Please enter the password for table.")
        else
            builder.setMessage("Please create a password for table.")

        val passCodeField = EditText(getContext())
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        passCodeField.layoutParams = lp
        builder.setView(passCodeField)

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            val a = passCodeField.text.toString()
            if(table.isProtected && passCodeField.text.toString() == table.passCode) {
                val joinTableRequest = JoinTableRequest(user.userId, user.username)
                val joinTableUrl = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name + "/join"
                request.POST(joinTableUrl, joinTableRequest, user.token, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                val intent = Intent(getContext(), TableActivity::class.java)
                                intent.putExtra("restaurantId", restaurant.id)
                                intent.putExtra("restaurantName", restaurant.name)
                                intent.putExtra("tableName", table.name)
                                intent.putExtra("userName", user.username)
                                startActivity(intent)
                                finish()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("User can not added.")
                    }
                })

            } else if(!table.isProtected) {
                var url = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name
                request.POST(url, a, user.token, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val joinTableRequest = JoinTableRequest(user.userId, user.username)
                        val joinTableUrl = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name + "/join"
                        request.POST(joinTableUrl, joinTableRequest, user.token, object: Callback {
                            override fun onResponse(call: Call?, response: Response) {
                                runOnUiThread {
                                    try {
                                        val intent = Intent(getContext(), TableActivity::class.java)
                                        intent.putExtra("restaurantId", restaurant.id)
                                        intent.putExtra("restaurantName", restaurant.name)
                                        intent.putExtra("tableName", table.name)
                                        intent.putExtra("userName", user.username)
                                        startActivity(intent)
                                        finish()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            override fun onFailure(call: Call?, e: IOException?) {
                                println("User can not added.")
                            }
                        })
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Passcode can not updated.")
                    }
                })
            }

        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->

        }

        builder.show()
    }
}
