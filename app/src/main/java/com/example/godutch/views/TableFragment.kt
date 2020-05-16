package com.example.godutch.views

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
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

class TableFragment : Fragment() {

    var ordersListView : ListView? = null
    var swipeToRefresh : SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: FragmentActivity? = activity
        val client = OkHttpClient()
        val request = OkHttpRequest(client)


        var adapter: TableOrdersListAdapter?

        val args = arguments
        val restaurantId = args!!.getString("restaurantId")
        val restaurantName = args!!.getString("restaurantName")
        val tableName = args!!.getString("tableName")
        val token = args!!.getString("token")

        var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName

        request.GET(url, token, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                activity!!.runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        println("Request Successful!!")
                        val users : JSONArray = json["users"] as JSONArray

                        adapter = TableOrdersListAdapter(activity, restaurantId, tableName)

                        for (i in 0 until users.length()) {
                            val user = users.getJSONObject(i)
                            adapter!!.addTableUser(TableUser(user["id"] as String, user["username"] as String, user["orders"] as JSONArray, user["paid"] as Boolean))

                            val orders : JSONArray = user["orders"] as JSONArray

                            for (j in 0 until orders.length()) {
                                val order = orders.getJSONObject(j)
                                val item = order["item"] as JSONObject
                                adapter!!.addItem(Order(MenuItem(item["name"] as String, item["price"] as Double), order["count"] as Int))
                            }

                            ordersListView!!.adapter = adapter

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_table, container, false)
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
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
