package com.example.godutch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.godutch.R
import com.example.godutch.models.Order
import okhttp3.OkHttpClient
import java.util.*

class MyOrdersListAdapter(context: Context) : BaseAdapter() {

    private val mData = ArrayList<Order>()

    private val client : OkHttpClient = OkHttpClient()
    private val request : OkHttpRequest = OkHttpRequest(client)

    private val preferences = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
    private val token = preferences.getString("token", "")
    private val userId = preferences.getString("userId", "")

    private val mInflater: LayoutInflater = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun addItem(item: Order) {
        mData.add(item)
        notifyDataSetChanged()
    }

    fun removeAtPosition(position: Int) {
        mData.removeAt(position)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): Order {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder? = null

        if (convertView == null) {
            holder = ViewHolder()
            convertView = mInflater.inflate(R.layout.orders_list_item, null)
            holder.name = convertView!!.findViewById(R.id.order_name) as TextView
            holder.price = convertView.findViewById(R.id.order_price) as TextView
            holder.quantity = convertView.findViewById(R.id.order_count) as TextView

            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        holder.name!!.text = mData[position].item.name
        holder.price!!.text = "" + mData[position].item.price + " TL"
        holder.quantity!!.text = mData[position].count.toString()

        return convertView
    }

    class ViewHolder {
        var name: TextView? = null
        var price: TextView? = null
        var quantity: TextView? = null
    }


}