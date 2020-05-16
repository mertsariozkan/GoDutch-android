package com.example.godutch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.godutch.Payload.Requests.AddOrderRequest
import com.example.godutch.R
import com.example.godutch.models.MenuItem
import com.example.godutch.models.Order
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.*


class MenuListAdapter(context: Context, private val restaurantId: String, private val tableName: String) : BaseAdapter() {

    private val mData = ArrayList<MenuItem>()
    private val sectionHeader = TreeSet<Int>()

    private val client : OkHttpClient = OkHttpClient()
    private val request : OkHttpRequest = OkHttpRequest(client)

    private val preferences = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
    private val token = preferences.getString("token", "")
    private val userId = preferences.getString("userId", "")

    private val mInflater: LayoutInflater = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun addItem(item: MenuItem) {
        mData.add(item)
        notifyDataSetChanged()
    }

    fun addSectionHeaderItem(item: MenuItem) {
        mData.add(item)
        sectionHeader.add(mData.size - 1)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (sectionHeader.contains(position)) TYPE_HEADER else TYPE_ITEM
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): MenuItem {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null
        val rowType = getItemViewType(position)

        if (convertView == null) {
            holder = ViewHolder()
            when (rowType) {
                TYPE_ITEM -> {
                    convertView = mInflater.inflate(R.layout.menulist_row_item, null)
                    holder.name = convertView!!.findViewById(R.id.item_name) as TextView
                    holder.price = convertView.findViewById(R.id.order_price) as TextView
                    holder.addButton = convertView.findViewById(R.id.item_add_button) as ImageView
                }
                TYPE_HEADER -> {
                    convertView = mInflater.inflate(R.layout.menulist_header_item, null)
                    holder.name = convertView!!.findViewById(R.id.menu_category_name) as TextView
                }
            }
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        if (rowType == TYPE_ITEM) {
            holder.name!!.setText(mData[position].name)
            holder.price!!.text = "" + mData[position].price + " TL"
            holder.addButton!!.setOnClickListener {
                val url = AppCommons.RootUrl + "order/" + restaurantId + "/" + tableName + "/" + userId
                val addOrderRequest = AddOrderRequest(Order(MenuItem(mData[position].name, mData[position].price), 1))
                request.POST(url, addOrderRequest, token, object: Callback {
                    override fun onResponse(call: Call?, response: Response) {
                        println("Order added.")
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        println("Order can not added.")
                    }
                })
            }
        } else if (rowType == TYPE_HEADER) {
            holder.name!!.setText(mData[position].name)
        }


        return convertView
    }

    class ViewHolder {
        var name: TextView? = null
        var price: TextView? = null
        var addButton: ImageView? = null
    }

    companion object {

        private val TYPE_ITEM = 0
        private val TYPE_HEADER = 1
    }
}