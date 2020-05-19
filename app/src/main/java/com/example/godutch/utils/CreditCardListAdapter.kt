package com.example.godutch.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.example.godutch.R
import com.example.godutch.models.CardData



class CreditCardListAdapter(context: Context, @LayoutRes private val layoutResource: Int, private val creditCards: ArrayList<CardData>):
    ArrayAdapter<CardData>(context, layoutResource, creditCards), Filterable {

    private var mCreditCards: ArrayList<CardData> = creditCards

    override fun getCount(): Int {
        return mCreditCards.size
    }

    override fun getItem(p0: Int): CardData? {
        return mCreditCards[p0]
    }

    fun removeAtPosition(position: Int) {
        mCreditCards.removeAt(position)
        notifyDataSetChanged()
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var listItem: View? = convertView
        if (listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.creditcard_row, parent, false)

        val creditCard = mCreditCards.get(position)

        val cardName = listItem!!.findViewById(R.id.cardName) as TextView
        cardName.text = creditCard.cardAlias

        val cardNo = listItem.findViewById(R.id.cardNo) as TextView
        cardNo.text = "**** **** **** " + creditCard.lastFourDigits

        return listItem

    }




}