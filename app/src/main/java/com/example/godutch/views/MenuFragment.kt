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
import com.example.godutch.models.Restaurant
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.MenuListAdapter
import com.example.godutch.utils.OkHttpRequest
import com.example.godutch.utils.RestaurantsListAdapter
import kotlinx.android.synthetic.main.fragment_menu.view.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MenuFragment : Fragment() {

    var menuListView : ListView? = null
    var swipeToRefresh : SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity: FragmentActivity? = activity
        val client = OkHttpClient()
        val request = OkHttpRequest(client)


        var adapter: MenuListAdapter?

        val args = arguments
        val restaurantId = args!!.getString("restaurantId")
        val restaurantName = args!!.getString("restaurantName")
        val tableName = args!!.getString("tableName")
        val token = args!!.getString("token")

        var url = AppCommons.RootUrl + "menu/" + restaurantId

        request.GET(url, token, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                activity!!.runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        println("Request Successful!!")

                        val menuCategories = json["menuCategories"] as JSONArray

                        adapter = MenuListAdapter(activity, restaurantId, tableName)

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
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

}
