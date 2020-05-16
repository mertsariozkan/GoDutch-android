package com.example.godutch.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextMenu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.Payload.Requests.DeleteOrderRequest
import com.example.godutch.R
import com.example.godutch.models.MenuItem
import com.example.godutch.models.Order
import com.example.godutch.models.TableUser
import com.example.godutch.models.UserSpecifiedOrder
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.WaiterOrdersListAdapter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException


class WaiterTableActivity : AppCompatActivity() {

    val client = OkHttpClient()
    val request = OkHttpRequest(client)
    var token : String? = null
    var restaurantId : String? = null
    var tableName : String? = null
    var ordersListView : ListView? = null
    var adapter: WaiterOrdersListAdapter? = null
    var totalAmountText: TextView? = null
    var totalAmount: Double = 0.00

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiter_table)

        val swipeToRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeToRefreshOrders)
        swipeToRefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        swipeToRefresh.setColorSchemeColors(Color.WHITE)
        swipeToRefresh.setOnRefreshListener {
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            swipeToRefresh!!.isRefreshing = false
        }

        ordersListView = findViewById<ListView>(R.id.waiter_ordersListView)
        totalAmountText = findViewById<TextView>(R.id.waiter_totalAmountValue)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        token = preferences.getString("token", "")

        restaurantId = intent.getStringExtra("restaurantId")
        tableName = intent.getStringExtra("tableName")

        var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName

        request.GET(url, token!!, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        println("Request Successful!!")
                        val users : JSONArray = json["users"] as JSONArray

                        adapter = WaiterOrdersListAdapter(this@WaiterTableActivity, this@WaiterTableActivity, restaurantId!!, tableName!!)
                        totalAmount = 0.00
                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            adapter!!.addTableUser(TableUser(user["id"] as String, user["username"] as String, user["orders"] as JSONArray, user["paid"] as Boolean))

                            val orders : JSONArray = user["orders"] as JSONArray

                            for (j in 0 until orders.length()) {
                                val order = orders.getJSONObject(j)
                                val item = order["item"] as JSONObject
                                adapter!!.addItem(UserSpecifiedOrder(user["id"] as String, MenuItem(item["name"] as String, item["price"] as Double), order["count"] as Int))

                                totalAmount += (item["price"] as Double)
                            }

                            totalAmountText!!.text = "$totalAmount TL"
                            ordersListView!!.adapter = adapter
                            registerForContextMenu(ordersListView!!)

                        }

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

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.waiter_ordersListView) {
            val lv = v as ListView
            val acmi = menuInfo as AdapterView.AdapterContextMenuInfo?
            val order = lv.getItemAtPosition(acmi!!.position) as UserSpecifiedOrder

            menu.add("Delete")
        }
    }

    override fun onContextItemSelected(item: android.view.MenuItem): Boolean {
        if (item.title == "Delete") {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this order?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    var info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                    var order = ordersListView!!.getItemAtPosition(info.position) as UserSpecifiedOrder
                    val url = AppCommons.RootUrl + "order/" + restaurantId + "/" + tableName + "/" + order.userId
                    val deleteOrderRequest = DeleteOrderRequest(order.item.name)
                    request.DELETE(url, deleteOrderRequest, token!!, object : Callback {
                        override fun onResponse(call: Call?, response: Response) {
                            runOnUiThread {
                                adapter!!.removeAtPosition(info.position)
                                totalAmount -= order.item.price
                                totalAmountText!!.text = "$totalAmount TL"
                            }
                            println("Order deleted.")
                        }

                        override fun onFailure(call: Call?, e: IOException?) {
                            println("Order can not deleted.")
                        }
                    })
                }.setNegativeButton(android.R.string.no, null).show()
        }
        return true
    }
}