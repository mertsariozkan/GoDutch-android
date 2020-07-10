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
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.MenuListAdapter
import com.example.godutch.utils.OkHttpRequest
import kotlinx.android.synthetic.main.fragment_menu.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MenuFragment : Fragment() {

    var menuListView : ListView? = null
    var swipeToRefresh : SwipeRefreshLayout? = null
    var progressBar : ProgressBar? = null
    var timer : Timer? = null

    var restaurantId: String? = null
    var restaurantName: String? = null
    var tableName: String? = null
    var token: String? = null
    var userId: String? = null

    val client = OkHttpClient()
    val request = OkHttpRequest(client)

    var adapter : MenuListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: FragmentActivity? = activity

        val args = arguments
        restaurantId = args!!.getString("restaurantId")
        restaurantName = args!!.getString("restaurantName")
        tableName = args!!.getString("tableName")
        token = args!!.getString("token")
        userId = args.getString("userId")

        var url = AppCommons.RootUrl + "menu/" + restaurantId

        request.GET(url, token!!, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                activity!!.runOnUiThread {
                    try {
                        progressBar!!.visibility = ProgressBar.INVISIBLE
                        val json = JSONObject(responseData)
                        println("Request Successful!!")

                        val menuCategories = json["menuCategories"] as JSONArray

                        adapter = MenuListAdapter(activity, restaurantId!!, tableName!!)

                        for (i in 0 until menuCategories.length()) {
                            val category = menuCategories.getJSONObject(i)
                            val items = category["items"] as JSONArray
                            adapter!!.addSectionHeaderItem(MenuItem(category["categoryName"] as String, 0.0))
                            for(j in 0 until items.length()) {
                                val item = items.getJSONObject(j)
                                adapter!!.addItem(MenuItem(item["name"] as String, item["price"] as Double))
                            }
                        }

                        menuListView!!.adapter = adapter
                        startTimer()
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
        val view = inflater.inflate(R.layout.fragment_menu, container, false)
        progressBar = view.f_menu_progressbar
        progressBar!!.visibility = ProgressBar.VISIBLE
        menuListView = view.menuListView
        swipeToRefresh = view.swipeToRefreshMenu
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

    override fun onPause() {
        super.onPause()
        stopTimer()
    }

    override fun onResume() {
        super.onResume()
        startTimer()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
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
                                    val json = JSONObject(responseData)
                                    println("Request Successful!!")
                                    val users: JSONArray = json["users"] as JSONArray

                                    if(json["paymentActive"] as Boolean) {
                                        adapter!!.notifyPaymentInitiation()
                                        (activity as TableActivity).invalidateOptionsMenu()
                                    }

                                    var userInTable = false
                                    for (i in 0 until users.length()) {
                                        val user = users.getJSONObject(i)
                                        if (user["id"] == userId)
                                            userInTable = true
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
        if(timer != null) {
            timer!!.cancel()
            timer!!.purge()
            timer = null
        }
    }

}
