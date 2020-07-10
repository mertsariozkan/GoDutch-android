package com.example.godutch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.godutch.R
import com.example.godutch.models.Order
import com.example.godutch.models.TableUser
import okhttp3.OkHttpClient
import java.util.*


class TableOrdersListAdapter(context: Context, private val restaurantId: String, private val tableName: String) : BaseAdapter() {

    private val mData = ArrayList<Any>()
    private val tableUserSet = TreeSet<Int>()

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

    fun addTableUser(item: TableUser) {
        mData.add(item)
        tableUserSet.add(mData.size - 1)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (tableUserSet.contains(position)) TYPE_USER else TYPE_ORDER
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: ViewHolder?
        val rowType = getItemViewType(position)

        if (convertView == null) {
            holder = ViewHolder()
            when (rowType) {
                TYPE_ORDER -> {
                    convertView = mInflater.inflate(R.layout.orders_list_item, null)
                    holder.name = convertView!!.findViewById(R.id.order_name) as TextView
                    holder.price = convertView.findViewById(R.id.order_price) as TextView
                    holder.quantity = convertView.findViewById(R.id.order_count) as TextView
                }
                TYPE_USER -> {
                    convertView = mInflater.inflate(R.layout.tableorderslist_user_item, null)
                    holder.name = convertView!!.findViewById(R.id.table_user_name) as TextView
                }
            }
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        if (rowType == TYPE_ORDER) {
            holder.name!!.text = (mData[position] as Order).item.name
            holder.price!!.text = "" + (mData[position] as Order).item.price + " TL"
            holder.quantity!!.text = (mData[position] as Order).count.toString()
        } else if (rowType == TYPE_USER) {
            holder.name!!.text = (mData[position] as TableUser).username
        }


        return convertView
    }

    class ViewHolder {
        var name: TextView? = null
        var price: TextView? = null
        var quantity: TextView? = null
    }

    companion object {

        private val TYPE_ORDER = 0
        private val TYPE_USER = 1
    }
}