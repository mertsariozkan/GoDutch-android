package com.example.godutch.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.Payload.Responses.GetAllTablesResponse
import com.example.godutch.R
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.SortingHelper
import com.example.godutch.utils.TablesListAdapter
import kotlinx.android.synthetic.main.activity_table_list.*
import kotlinx.android.synthetic.main.appbar_godutch.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.prefs.Preferences
import kotlin.collections.ArrayList


class TableListActivity : AppCompatActivity() {

    var tablesListView: ListView? = null

    val client: OkHttpClient = OkHttpClient()
    val request: OkHttpRequest = OkHttpRequest(client)
    var progressBar: ProgressBar? = null

    var activity: Activity? = null
    var token: String? = null
    var restaurantId: String? = null
    var url: String? = null
    var adapter: TablesListAdapter? = null
    val adapterLock = Any()
    lateinit var preferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_table_list)
        progressBar = findViewById(R.id.tablelist_progressbar)
        progressBar!!.visibility = ProgressBar.VISIBLE

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.appbar_godutch)

        preferences = this.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)

        val userName = preferences.getString("username","")

        actionbar_username.text = userName + ""
        actionbar_profile.visibility = View.VISIBLE

        activity = this

        token = preferences.getString("token", "")
        restaurantId = preferences.getString("restaurantId", "")
        url = AppCommons.RootUrl + "table/" + restaurantId + "/allactive"

        val tableSearchField = findViewById<SearchView>(R.id.tableSearchField)
        tablesListView = findViewById(R.id.tablesListView)


        val swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeToRefreshTables)
        swipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorPrimary
            )
        )
        swipeToRefresh.setColorSchemeColors(Color.WHITE)
        swipeToRefresh.setOnRefreshListener {
            createListviewData()
            swipeToRefresh!!.isRefreshing = false
        }


        tablesListView!!.setOnItemClickListener { parent, view, position, id ->
            stopTimer()
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
                synchronized(adapterLock) {
                    adapter!!.filter.filter(text)
                }
                return false
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.appbar_table, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.leaveTableButton) {
            val builder = android.app.AlertDialog.Builder(this)

            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")

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
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.tablesListView) {
            val lv = v as ListView
            val acmi = menuInfo as AdapterView.AdapterContextMenuInfo?
            val table = lv.getItemAtPosition(acmi!!.position) as String
            menu.add("Reset Table")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.title == "Reset Table") {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to reset this table?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    var info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                    val url =
                        AppCommons.RootUrl + "table/" + restaurantId + "/" + tablesListView!!.getItemAtPosition(info.position) + "/reset"
                    request.POST(url, token!!, object : Callback {
                        override fun onResponse(call: Call?, response: Response) {
                            activity!!.runOnUiThread {
                                adapter!!.removeAtPosition(info.position)
                            }
                        }

                        override fun onFailure(call: Call?, e: IOException?) {

                        }
                    })
                }.setNegativeButton(android.R.string.no, null).show()
        }
        return true
    }


    private fun createListviewData() {
        request.GET(url!!, token!!, object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        progressBar!!.visibility = ProgressBar.INVISIBLE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

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
                        tableList = SortingHelper.sortList(tableList)

                        synchronized(adapterLock) {
                            adapter = TablesListAdapter(activity!!, android.R.layout.simple_list_item_1, tableList)
                            adapter!!.filter.filter(tableSearchField.query.toString())
                            tablesListView!!.adapter = adapter
                            registerForContextMenu(tablesListView)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    progressBar!!.visibility = ProgressBar.INVISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onResume() {
        super.onResume()
        startTimer()
    }

    private fun startTimer() {
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                createListviewData()
            }
        }, 0, 3000)
    }

    private fun stopTimer() {
        if (timer
            != null
        ) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

    companion object {
        var timer: Timer? = null
    }

}
