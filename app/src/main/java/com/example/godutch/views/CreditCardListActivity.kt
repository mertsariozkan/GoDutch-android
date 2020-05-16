package com.example.godutch.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ListView
import com.example.godutch.Payload.Responses.GetAllTablesResponse
import com.example.godutch.R
import com.example.godutch.models.CardData
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.CreditCardListAdapter
import com.example.godutch.utils.OkHttpRequest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CreditCardListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_card_list)

        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")
        val userId = preferences.getString("userId", "")

        val fab: View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener { view ->
            val intent = Intent(this, AddCreditCardActivity::class.java)
            startActivity(intent)
        }

        val creditCardListView: ListView = findViewById(R.id.creditCardsListView)

        val url = AppCommons.RootUrl + "card/" + userId
        request.GET(url, token, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                val responseData = response.body()?.string()
                runOnUiThread {
                    try {
                        val json = JSONArray(responseData)

                        var cardList = ArrayList<CardData>()
                        for (i in 0 until json.length()) {
                            val card = json.getJSONObject(i)
                            cardList.add(CardData(card["cardAlias"] as String, card["lastFourDigits"] as String, card["cardToken"] as String))
                        }
                        var adapter = CreditCardListAdapter(this@CreditCardListActivity, android.R.layout.simple_list_item_1, cardList)
                        creditCardListView.adapter = adapter

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
}
