package com.example.godutch.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.example.godutch.Payload.Requests.DeleteCardRequest
import com.example.godutch.Payload.Responses.GetAllTablesResponse
import com.example.godutch.R
import com.example.godutch.models.CardData
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.CreditCardListAdapter
import com.example.godutch.utils.OkHttpRequest
import kotlinx.android.synthetic.main.activity_credit_card_list.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class CreditCardListActivity : AppCompatActivity() {

    var userId : String? = null
    var token : String? = null
    var adapter : CreditCardListAdapter? = null

    val client : OkHttpClient = OkHttpClient()
    val request : OkHttpRequest = OkHttpRequest(client)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_card_list)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        token = preferences.getString("token", "")
        userId = preferences.getString("userId", "")

        val fab: View = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener { view ->
            val intent = Intent(this, AddCreditCardActivity::class.java)
            startActivity(intent)
        }

        val creditCardListView: ListView = findViewById(R.id.creditCardsListView)

        val url = AppCommons.RootUrl + "card/" + userId
        request.GET(url, token!!, object: Callback {
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
                        adapter = CreditCardListAdapter(this@CreditCardListActivity, android.R.layout.simple_list_item_1, cardList)
                        creditCardListView.adapter = adapter

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

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (v.id == R.id.tablesListView) {
            val lv = v as ListView
            val acmi = menuInfo as AdapterView.AdapterContextMenuInfo?
            val card = lv.getItemAtPosition(acmi!!.position) as CardData
            menu.add("Delete")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.title == "Delete") {
            AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this card?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                    val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
                    val url = AppCommons.RootUrl + "card/" + userId

                    val deleteCardRequest = DeleteCardRequest((creditCardsListView!!.getItemAtPosition(info.position) as CardData).cardToken)
                    request.POST(url, deleteCardRequest, token!!, object : Callback {
                        override fun onResponse(call: Call?, response: Response) {
                            this@CreditCardListActivity.runOnUiThread {
                                adapter!!.removeAtPosition(info.position)
                            }
                        }

                        override fun onFailure(call: Call?, e: IOException?) { }
                    })
                }.setNegativeButton(android.R.string.no, null).show()
        }
        return true
    }
}
