package com.example.godutch.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.Payload.Requests.DeleteOrderRequest
import com.example.godutch.Payload.Requests.DeleteUserRequest
import com.example.godutch.R
import com.example.godutch.models.MenuItem
import com.example.godutch.models.Order
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.MyOrdersListAdapter
import com.example.godutch.utils.OkHttpRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_orders.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException

class OrdersFragment : Fragment() {

    var ordersListView: ListView? = null
    var swipeToRefresh: SwipeRefreshLayout? = null
    var totalAmountLabel: TextView? = null

    var restaurantId: String? = null
    var restaurantName: String? = null
    var tableName: String? = null
    var token: String? = null
    var userId: String? = null

    val client = OkHttpClient()
    val request = OkHttpRequest(client)


    var adapter: MyOrdersListAdapter? = null

    var savedInstanceState : Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState
        val activity: FragmentActivity? = activity
        val client = OkHttpClient()
        val request = OkHttpRequest(client)


        val args = arguments
        restaurantId = args!!.getString("restaurantId")
        restaurantName = args.getString("restaurantName")
        tableName = args.getString("tableName")
        token = args.getString("token")
        userId = args.getString("userId")

        var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName
        request.GET(url, token!!, object : Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                activity!!.runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        println("Request Successful!!")
                        var users: JSONArray = json["users"] as JSONArray

                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            if (user["id"] == userId) {
                                val orders: JSONArray = user["orders"] as JSONArray

                                adapter = MyOrdersListAdapter(activity)
                                var totalAmount: Double = 0.0
                                for (j in 0 until orders.length()) {
                                    val order = orders.getJSONObject(j)
                                    val item = order["item"] as JSONObject
                                    adapter!!.addItem(
                                        Order(
                                            MenuItem(
                                                item["name"] as String,
                                                (item["price"] as Double) * (order["count"] as Int)
                                            ), order["count"] as Int
                                        )
                                    )
                                    totalAmount += (item["price"] as Double)
                                }
                                totalAmountLabel!!.text = "$totalAmount TL"
                                ordersListView!!.adapter = adapter
                                registerForContextMenu(ordersListView!!)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                println("Can not get tables.")
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        ordersListView = view.my_orders_listview
        swipeToRefresh = view.swipeToRefreshOrders
        swipeToRefresh!!.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                activity!!.applicationContext,
                R.color.colorPrimary
            )
        )
        swipeToRefresh!!.setColorSchemeColors(Color.WHITE)
        swipeToRefresh!!.setOnRefreshListener {
            onCreate(savedInstanceState)
            swipeToRefresh!!.isRefreshing = false
        }

        totalAmountLabel = view.totalAmountValue

        return view
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.my_orders_listview) {
            val lv = v as ListView
            val acmi = menuInfo as AdapterView.AdapterContextMenuInfo?
            val order = lv.getItemAtPosition(acmi!!.position) as Order

            menu.add("Delete")
        }
    }

    override fun onContextItemSelected(item: android.view.MenuItem): Boolean {
        if (item.title == "Delete") {
            AlertDialog.Builder(this.context!!)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this order?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    val url = AppCommons.RootUrl + "order/" + restaurantId + "/" + tableName + "/" + userId
                    var info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                    val deleteOrderRequest = DeleteOrderRequest((ordersListView!!.getItemAtPosition(info.position) as Order).item.name)
                    request.DELETE(url, deleteOrderRequest, token!!, object : Callback {
                        override fun onResponse(call: Call?, response: Response) {
                            activity!!.runOnUiThread {
                                adapter!!.removeAtPosition(info.position)
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


    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("onattach")
    }

    override fun onStart() {
        super.onStart()
        println("onstart")
    }

    override fun onDetach() {
        super.onDetach()
    }

}
