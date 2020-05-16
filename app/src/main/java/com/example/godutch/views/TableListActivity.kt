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
import com.example.godutch.R
import com.example.godutch.models.Restaurant
import com.example.godutch.models.RestaurantTable
import com.example.godutch.models.UserConfigDto
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.TablesListAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class TableListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_list)

        val activity: Activity = this
        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")
        val userId = preferences.getString("userId", "")
        val username = preferences.getString("username", "")
        var restaurantId = preferences.getString("restaurantId", "")

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        val tableSearchField = findViewById<SearchView>(R.id.tableSearchField)
        val tablesListView = findViewById<ListView>(R.id.tablesListView)
        var adapter : TablesListAdapter? = null
        val tableList = ArrayList<String>()

        var url = AppCommons.RootUrl + "table/" + restaurantId + "/allactive"
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

                        for (i in 0 until result.restaurantTables.length()) {
                            val table = result.restaurantTables.getJSONObject(i)
                            tableList.add(table["name"] as String)
                        }

                        adapter = TablesListAdapter(activity, android.R.layout.simple_list_item_1, tableList)
                        tablesListView.adapter = adapter

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Can not get tables.")
            }
        })


        tablesListView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, WaiterTableActivity::class.java)
            intent.putExtra("restaurantId", restaurantId)
            intent.putExtra("tableName", parent.getItemAtPosition(position) as String)
            startActivity(intent)
        }

        tableSearchField.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                val text = newText
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

}
