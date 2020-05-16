package com.example.godutch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes

class TablesListAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val tables: List<String>):
    ArrayAdapter<String>(context, layoutResource, tables), Filterable {

    private var mTables: List<String> = tables

    override fun getCount(): Int {
        return mTables.size
    }

    override fun getItem(p0: Int): String? {
        return mTables[p0]
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createViewFromResource(position, convertView, parent)
    }

    private fun createViewFromResource(position: Int, convertView: View?, parent: ViewGroup?): View{
        val view: TextView = convertView as TextView? ?: LayoutInflater.from(context).inflate(layoutResource, parent, false) as TextView
        view.text = mTables[position]
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                mTables = filterResults.values as List<String>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                val filterResults = FilterResults()
                filterResults.values = if (queryString==null || queryString.isEmpty())
                    tables
                else
                    tables.filter {
                        it.toLowerCase().contains(queryString)
                    }
                return filterResults
            }
        }
    }
}