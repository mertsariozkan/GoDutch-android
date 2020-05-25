package com.example.godutch.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.godutch.Payload.Requests.AddOrderRequest
import com.example.godutch.Payload.Requests.DeleteUserRequest
import com.example.godutch.R
import com.example.godutch.models.*
import com.example.godutch.views.RestaurantsActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.*


class WaiterOrdersListAdapter(context: Context, private val activity: Activity, private val restaurantId: String, private val tableName: String, private var paymentInitiated: Boolean = false) : BaseAdapter() {

    private val mData = ArrayList<Any>()
    private val tableUserSet = TreeSet<Int>()

    private val client : OkHttpClient = OkHttpClient()
    private val request : OkHttpRequest = OkHttpRequest(client)

    private val preferences = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
    private val token = preferences.getString("token", "")
    private val userId = preferences.getString("userId", "")

    private val mInflater: LayoutInflater = context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    fun addItem(item: UserSpecifiedOrder) {
        mData.add(item)
        notifyDataSetChanged()
    }

    fun addTableUser(item: TableUser) {
        mData.add(item)
        tableUserSet.add(mData.size - 1)
        notifyDataSetChanged()
    }

    fun removeAtPosition(position: Int) {
        mData.removeAt(position)
        notifyDataSetChanged()
    }

    fun notifyPaymentInitiation() {
        paymentInitiated = true
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
                    convertView = mInflater.inflate(R.layout.waiter_tableorderlist_user_item, null)
                    holder.name = convertView!!.findViewById(R.id.table_user_name) as TextView
                    holder.deleteUserButton = convertView!!.findViewById(R.id.deleteUserButton) as ImageButton
                }
            }
            convertView!!.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        if (rowType == TYPE_ORDER) {
            holder.name!!.text = (mData[position] as UserSpecifiedOrder).item.name
            holder.price!!.text = "" + (mData[position] as UserSpecifiedOrder).item.price + " TL"
            holder.quantity!!.text = (mData[position] as UserSpecifiedOrder).count.toString()
        } else if (rowType == TYPE_USER) {
            holder.name!!.text = (mData[position] as TableUser).username
            holder.deleteUserButton!!.setOnClickListener {
                AlertDialog.Builder(activity)
                    .setTitle("Confirmation")
                    .setMessage("Are you sure you want to remove this user from table?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                        val url = AppCommons.RootUrl + "table/" + restaurantId + "/" + tableName
                        request.DELETE(
                            url,
                            DeleteUserRequest((mData[position] as TableUser).id, (mData[position] as TableUser).username),
                            token!!,
                            object : Callback {
                                override fun onResponse(call: Call?, response: Response) {
                                    activity.runOnUiThread {
                                        try {
                                            activity.finish()
                                            activity.overridePendingTransition(0, 0)
                                            activity.startActivity(activity.intent)
                                            activity.overridePendingTransition(0, 0)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }

                                override fun onFailure(call: Call?, e: IOException?) {
                                    println("Can not get table orders.")
                                }
                            })
                    }.setNegativeButton(android.R.string.no, null).show()
            }

            if(paymentInitiated) {
                if((mData[position] as TableUser).isPaid) {
                    holder.name!!.background = ContextCompat.getDrawable(activity, R.drawable.green_underline)
                } else {
                    holder.name!!.background = ContextCompat.getDrawable(activity, R.drawable.red_underline)
                }
            }

        }


        return convertView
    }

    class ViewHolder {
        var name: TextView? = null
        var price: TextView? = null
        var quantity: TextView? = null
        var deleteUserButton: ImageButton? = null
    }

    companion object {

        private val TYPE_ORDER = 0
        private val TYPE_USER = 1
    }
}