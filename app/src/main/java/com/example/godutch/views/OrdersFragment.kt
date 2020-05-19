package com.example.godutch.views

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.godutch.Payload.Requests.DeleteOrderRequest
import com.example.godutch.Payload.Requests.PaymentRequest
import com.example.godutch.R
import com.example.godutch.models.CardData
import com.example.godutch.models.MenuItem
import com.example.godutch.models.Order
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.CreditCardListAdapter
import com.example.godutch.utils.MyOrdersListAdapter
import com.example.godutch.utils.OkHttpRequest
import kotlinx.android.synthetic.main.fragment_orders.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class OrdersFragment : Fragment() {

    var timer: Timer? = null

    var ordersListView: ListView? = null
    var swipeToRefresh: SwipeRefreshLayout? = null
    var totalAmountLabel: TextView? = null
    var progressBar: ProgressBar? = null
    var payButton: Button? = null
    var payLayout: LinearLayout? = null
    var cardSpinner : Spinner? = null

    var restaurantId: String? = null
    var restaurantName: String? = null
    var tableName: String? = null
    var token: String? = null
    var userId: String? = null
    var username: String? = null

    val client = OkHttpClient()
    val request = OkHttpRequest(client)


    var adapter: MyOrdersListAdapter? = null

    var savedInstanceState: Bundle? = null

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
        username = args.getString("userName")

        val getCardsUrl = AppCommons.RootUrl + "card/" + userId
        request.GET(getCardsUrl, token!!, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                activity!!.runOnUiThread {
                    try {
                        val json = JSONArray(responseData)

                        var cardList = ArrayList<String>()
                        for (i in 0 until json.length()) {
                            val card = json.getJSONObject(i)
                            cardList.add(card["cardAlias"] as String)
                        }
                        val adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, cardList)
                        cardSpinner!!.adapter = adapter

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            override fun onFailure(call: Call?, e: IOException?) {
                println("Can not get cards.")
            }
        })

        startTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        progressBar = view.f_order_progressBar
        progressBar!!.visibility = ProgressBar.VISIBLE
        activity!!.window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
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

        cardSpinner = view.spinner

        totalAmountLabel = view.totalAmountValue

        payButton = view.payButton
        payLayout = view.pay_layout

        payButton!!.setOnClickListener {
            val totalAmountStr = totalAmountLabel!!.text.toString()
            val totalAmount = totalAmountStr.substring(0, totalAmountStr.length - 3).toDouble()

            val paymentRequest = PaymentRequest(
                restaurantId!!,
                username!!,
                tableName!!,
                totalAmount,
                cardSpinner!!.selectedItem.toString()
            )

            payButton!!.isClickable = false
            payLayout!!.background = ContextCompat.getDrawable(activity!!, R.drawable.grayfill)

            val paymentUrl = AppCommons.RootUrl + "payment"
            request.POST(paymentUrl, paymentRequest, token!!, object: Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val responseData = response.body()?.string()
                    val json = JSONObject(responseData)
                    activity!!.runOnUiThread {
                        try {
                            if(json["success"] as Boolean) {
                                android.app.AlertDialog.Builder(activity!!)
                                    .setMessage("Payment completed successfully.")
                                    .setIcon(android.R.drawable.ic_dialog_info)
                                    .setPositiveButton(android.R.string.ok, null).show()
                            } else {
                                payButton!!.isClickable = true
                                payLayout!!.background = ContextCompat.getDrawable(activity!!, R.drawable.greenfill)
                                android.app.AlertDialog.Builder(activity!!)
                                    .setMessage("Payment failed. Please try with another registered card.")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.ok, null).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call?, e: IOException?) {
                    println("Can not get cards.")
                }
            })
        }

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
                    val deleteOrderRequest =
                        DeleteOrderRequest((ordersListView!!.getItemAtPosition(info.position) as Order).item.name)
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
        stopTimer()
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
                var url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName
                request.GET(url, token!!, object : Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        val responseData = response.body()?.string()
                        if (activity != null) {
                            activity!!.runOnUiThread {
                                try {
                                    progressBar!!.visibility = ProgressBar.INVISIBLE
                                    activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    val json = JSONObject(responseData)
                                    println("Request Successful!!")
                                    val users: JSONArray = json["users"] as JSONArray

                                    var userInTable = false
                                    for (i in 0 until users.length()) {
                                        val user = users.getJSONObject(i)
                                        if (user["id"] == userId) {
                                            userInTable = true
                                            val orders: JSONArray = user["orders"] as JSONArray

                                            adapter = MyOrdersListAdapter(activity!!)
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
                                                totalAmount += (item["price"] as Double) * (order["count"] as Int)
                                            }
                                            totalAmountLabel!!.text = "$totalAmount TL"
                                            ordersListView!!.adapter = adapter

                                            if((json["paymentActive"] as Boolean) && !(user["paid"] as Boolean)) {
                                                payButton!!.isClickable = true
                                                payLayout!!.background = ContextCompat.getDrawable(activity!!, R.drawable.greenfill)
                                                (activity as TableActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                                                unregisterForContextMenu(ordersListView!!)
                                            } else if(!(json["paymentActive"] as Boolean)) {
                                                payButton!!.isClickable = false
                                                payLayout!!.background = ContextCompat.getDrawable(activity!!, R.drawable.grayfill)
                                                registerForContextMenu(ordersListView!!)
                                            }
                                        }
                                    }
                                    if (!userInTable) {
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
                        println("Can not get tables.")
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
