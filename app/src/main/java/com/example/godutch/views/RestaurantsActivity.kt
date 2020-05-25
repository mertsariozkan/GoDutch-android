package com.example.godutch.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.Payload.Requests.JoinTableRequest
import com.example.godutch.Payload.Requests.PassCodeRequest
import com.example.godutch.Payload.Responses.GetAllTablesResponse
import com.example.godutch.Payload.Responses.GetRestaurantsResponse
import com.example.godutch.R
import com.example.godutch.models.Restaurant
import com.example.godutch.models.RestaurantTable
import com.example.godutch.models.UserConfigDto
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.RestaurantsListAdapter
import com.example.godutch.utils.SortingHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_restaurants.*
import kotlinx.android.synthetic.main.appbar_godutch.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException
import kotlin.math.pow
import kotlin.math.sqrt


class RestaurantsActivity : AppCompatActivity() {


    val client: OkHttpClient = OkHttpClient()
    val request: OkHttpRequest = OkHttpRequest(client)
    var restaurantsListView: ListView? = null
    var adapter: RestaurantsListAdapter? = null
    var progressBar: ProgressBar? = null
    lateinit var restaurantSuggestionLabel: TextView
    var closestRestaurant: Restaurant? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private lateinit var activity: Activity
    private lateinit var token : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurants)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 11)
        else {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    currentLocation = location
                }
        }

        progressBar = findViewById(R.id.restaurants_progressbar)
        startProgressBar()

        restaurantSuggestionLabel = findViewById(R.id.restaurantSuggestionLabel)

        activity = this

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        token = preferences.getString("token", "")
        val userId = preferences.getString("userId", "")
        val username = preferences.getString("username", "")

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        val restaurantSearchField = findViewById<SearchView>(R.id.restaurantSearchField)
        restaurantsListView = findViewById(R.id.restaurantsListView)

        var url = AppCommons.RootUrl + "restaurant/all"

        val swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeToRefreshRestaurants)
        swipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimary
            )
        )
        swipeToRefresh.setColorSchemeColors(Color.WHITE)
        swipeToRefresh.setOnRefreshListener {
            createListviewData(activity, url, token)
            swipeToRefresh!!.isRefreshing = false
        }


        createListviewData(activity, url, token)

        restaurantsListView!!.setOnItemClickListener { parent, view, position, id ->
            val restaurant = adapter?.getItem(position)
            val popup = PopupMenu(this, view)

            startProgressBar()

            url = AppCommons.RootUrl + "table/" + restaurant!!.id + "/all"
            request.GET(url, token, object : Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val responseData = response.body()?.string()
                    runOnUiThread {
                        try {
                            val json = JSONObject(responseData)
                            println("Request Successful!!")
                            var result = GetAllTablesResponse(
                                json["restaurantTableDtos"] as JSONArray
                            )
                            var tableList = ArrayList<String>()
                            for (i in 0 until result.restaurantTables.length()) {
                                val table = result.restaurantTables.getJSONObject(i)
                                tableList.add(table["name"] as String)
                            }
                            val sortedList = SortingHelper.sortList(tableList)
                            for (i in 0 until result.restaurantTables.length()) {
                                popup.menu.add(sortedList[i])
                            }
                            popup.show()
                            stopProgressBar()

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
                startProgressBar()
                url = AppCommons.RootUrl + "table/" + restaurant!!.id + "/" + item
                request.GET(url, token, object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                stopProgressBar()
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

        val restaurantSuggestionLayout = findViewById<LinearLayout>(R.id.restaurantSuggestionLayout)
        restaurantSuggestionLayout.setOnClickListener {
            val popup = PopupMenu(this, restaurantSuggestionLayout)
            startProgressBar()

            url = AppCommons.RootUrl + "table/" + closestRestaurant!!.id + "/all"
            request.GET(url, token, object : Callback {
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
                            stopProgressBar()

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
                startProgressBar()
                url = AppCommons.RootUrl + "table/" + closestRestaurant!!.id + "/" + item
                request.GET(url, token, object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                stopProgressBar()
                                val json = JSONObject(responseData)
                                println("Request Successful!!")
                                var table = RestaurantTable(
                                    json["name"] as String,
                                    json["protected"] as Boolean,
                                    json["passCode"] as String,
                                    json["users"] as JSONArray
                                )
                                var user = UserConfigDto(token, userId, username)
                                showPassCodeDialog(request, user, closestRestaurant!!, table)

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
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            11 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            currentLocation = location
                            var url = AppCommons.RootUrl + "restaurant/all"
                            createListviewData(activity, url, token)
                        }
                } else {
                    val restaurantSuggestionLayout = findViewById<LinearLayout>(R.id.restaurantSuggestionLayout)
                    restaurantSuggestionLayout.visibility = LinearLayout.GONE
                }
                return
            }
            else -> {

            }
        }
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

    private fun getContext(): Context {
        return this
    }

    private fun showPassCodeDialog(
        request: OkHttpRequest,
        user: UserConfigDto,
        restaurant: Restaurant,
        table: RestaurantTable
    ) {
        val builder = AlertDialog.Builder(getContext())
        builder.setTitle(restaurant.name + " - " + table.name)
        if (table.isProtected)
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
            startProgressBar()
            val passCodeRequest = PassCodeRequest(passCodeField.text.toString())
            if (table.isProtected && passCodeField.text.toString() == table.passCode) {
                val joinTableRequest = JoinTableRequest(user.userId, user.username)
                val joinTableUrl = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name + "/join"
                request.POST(joinTableUrl, joinTableRequest, user.token, object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        runOnUiThread {
                            try {
                                stopProgressBar()
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
                        stopProgressBar()
                    }
                })

            } else if (!table.isProtected) {
                var url = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name
                request.POST(url, passCodeRequest, user.token, object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val joinTableRequest = JoinTableRequest(user.userId, user.username)
                        val joinTableUrl = AppCommons.RootUrl + "table/" + restaurant.id + "/" + table.name + "/join"
                        request.POST(joinTableUrl, joinTableRequest, user.token, object : Callback {
                            override fun onResponse(call: Call?, response: Response) {
                                runOnUiThread {
                                    try {
                                        stopProgressBar()
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
                                stopProgressBar()
                            }
                        })
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Passcode can not updated.")
                        stopProgressBar()
                    }
                })
            } else {
                AlertDialog.Builder(this)
                    .setMessage("Table password is not correct.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, null).show()
                stopProgressBar()
            }
        }

        builder.setNegativeButton(android.R.string.no, null)

        builder.show()
    }

    private fun createListviewData(activity: Activity, url: String, token: String) {
        request.GET(url, token, object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        stopProgressBar()

                        val json = JSONObject(responseData)
                        println("Request Successful!!")
                        var result = GetRestaurantsResponse(
                            json["restaurants"] as JSONArray
                        )

                        val restaurantList = ArrayList<Restaurant>()

                        var closestDistance: Double? = null
                        for (i in 0 until result.restaurants.length()) {
                            val restaurant = result.restaurants.getJSONObject(i)
                            val restaurantObj = Restaurant(
                                restaurant["id"] as String,
                                restaurant["name"] as String,
                                restaurant["latitude"] as Double,
                                restaurant["longitude"] as Double
                            )
                            restaurantList.add(restaurantObj)

                            val distance = calculateDistance(restaurantObj.latitude, restaurantObj.longitude)
                            if (distance != null) {
                                if (closestDistance == null || closestDistance > distance) {
                                    closestDistance = distance
                                    closestRestaurant = restaurantObj
                                }
                            }
                        }

                        if (closestRestaurant != null)
                            restaurantSuggestionLabel.text = "Are you at " + closestRestaurant!!.name + " now?"

                        val sortedList = restaurantList.sortedWith(compareBy { it.name })
                        adapter = RestaurantsListAdapter(activity, android.R.layout.simple_list_item_1, sortedList)
                        restaurantsListView!!.adapter = adapter
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    stopProgressBar()
                }
                println("Can not get restaurants.")
            }
        })
    }

    private fun startProgressBar() {
        progressBar!!.visibility = ProgressBar.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun stopProgressBar() {
        progressBar!!.visibility = ProgressBar.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun calculateDistance(x: Double, y: Double): Double? {
        if (currentLocation != null) {
            return sqrt((currentLocation!!.latitude - x).pow(2) + (currentLocation!!.longitude - y).pow(2))
        }
        return null
    }


}
