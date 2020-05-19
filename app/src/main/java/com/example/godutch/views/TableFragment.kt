package com.example.godutch.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.R
import com.example.godutch.models.MenuItem
import com.example.godutch.models.Order
import com.example.godutch.models.TableUser
import com.example.godutch.utils.*
import kotlinx.android.synthetic.main.fragment_table.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class TableFragment : Fragment() {

    var ordersListView : ListView? = null
    var swipeToRefresh : SwipeRefreshLayout? = null
    var progressBar : ProgressBar? = null
    var timer: Timer? = null

    var restaurantId: String? = null
    var restaurantName: String? = null
    var tableName: String? = null
    var token: String? = null
    var userId: String? = null
    var adapter: TableOrdersListAdapter? = null

    val client = OkHttpClient()
    val request = OkHttpRequest(client)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: FragmentActivity? = activity
        val client = OkHttpClient()
        val request = OkHttpRequest(client)

        val args = arguments
        restaurantId = args!!.getString("restaurantId")
        restaurantName = args!!.getString("restaurantName")
        tableName = args!!.getString("tableName")
        token = args!!.getString("token")
        userId = args.getString("userId")

        startTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_table, container, false)

        progressBar = view.f_table_progressbar
        progressBar!!.visibility = ProgressBar.VISIBLE
        ordersListView = view.orderListView
        swipeToRefresh = view.swipeToRefreshOrders
        swipeToRefresh!!.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(activity!!.applicationContext, R.color.colorPrimary))
        swipeToRefresh!!.setColorSchemeColors(Color.WHITE)
        swipeToRefresh!!.setOnRefreshListener {
            onCreate(savedInstanceState)
            swipeToRefresh!!.isRefreshing = false
        }
        return view

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
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
                var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName

                request.GET(url, token!!, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        if(activity != null) {
                            activity!!.runOnUiThread {
                                try {
                                    val json = JSONObject(responseData)
                                    println("Request Successful!!")

                                    if(json["paymentActive"] as Boolean)
                                        (activity as TableActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

                                    val users: JSONArray = json["users"] as JSONArray

                                    adapter = TableOrdersListAdapter(activity!!, restaurantId!!, tableName!!)

                                    var userInTable = false

                                    for (i in 0 until users.length()) {
                                        val user = users.getJSONObject(i)

                                        if (user["id"] == userId)
                                            userInTable = true

                                        adapter!!.addTableUser(
                                            TableUser(
                                                user["id"] as String,
                                                user["username"] as String,
                                                user["orders"] as JSONArray,
                                                user["paid"] as Boolean
                                            )
                                        )

                                        val orders: JSONArray = user["orders"] as JSONArray

                                        for (j in 0 until orders.length()) {
                                            val order = orders.getJSONObject(j)
                                            val item = order["item"] as JSONObject
                                            adapter!!.addItem(
                                                Order(
                                                    MenuItem(
                                                        item["name"] as String,
                                                        item["price"] as Double
                                                    ), order["count"] as Int
                                                )
                                            )
                                        }

                                        ordersListView!!.adapter = adapter
                                        progressBar!!.visibility = ProgressBar.INVISIBLE
                                    }

                                    if(!userInTable) {
                                        stopTimer()
                                        Toast.makeText(activity, "You are removed from this table.", Toast.LENGTH_LONG).show()
                                        val intent = Intent(activity, RestaurantsActivity::class.java)
                                        startActivity(intent)
                                        activity!!.finish()
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Can not get table orders.")
                    }
                })
            }
        }, 0, 3000)
    }

    private fun stopTimer() {
        timer!!.cancel()
        timer!!.purge()
    }

}
