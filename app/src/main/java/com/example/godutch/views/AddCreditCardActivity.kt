package com.example.godutch.views

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.godutch.R
import com.braintreepayments.cardform.view.CardForm
import com.example.godutch.Payload.Requests.SaveCardRequest
import com.example.godutch.models.CardData
import com.example.godutch.utils.AppCommons
import com.example.godutch.utils.CreditCardListAdapter
import com.example.godutch.utils.OkHttpRequest
import kotlinx.android.synthetic.main.activity_add_credit_card.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class AddCreditCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_credit_card)

        val client : OkHttpClient = OkHttpClient()
        val request : OkHttpRequest = OkHttpRequest(client)

        val preferences = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE)
        val token = preferences.getString("token", "")
        val userId = preferences.getString("userId", "")
        val email = preferences.getString("email", "")

        val cardForm = findViewById<CardForm>(R.id.card_form)
        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .setup(this)

        val cardAliasField = findViewById<EditText>(R.id.cardAlias)
        val saveButton = findViewById<Button>(R.id.saveCardButton)
        saveButton.setOnClickListener { view ->

            val saveCardRequest = SaveCardRequest(
                cardAlias.text.toString(),
                cardForm.cardholderName,
                cardForm.cardNumber,
                cardForm.expirationMonth,
                cardForm.expirationYear,
                email,
                userId
            )

            val url = AppCommons.RootUrl + "card"
            request.POST(url, saveCardRequest, token, object: Callback {
                override fun onResponse(call: Call?, response: Response) {
                    val responseData = response.body()?.string()
                    val json = JSONObject(responseData)
                    runOnUiThread {
                        var alert = AlertDialog.Builder(this@AddCreditCardActivity)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(
                                android.R.string.ok
                            ) { dialog, whichButton ->
                                finish()
                            }
                        if(json["success"] as Boolean) {
                            alert.setTitle("Success")
                            alert.setMessage("Credit card saved successfully.")
                        } else {
                            alert.setTitle("Failure")
                            alert.setMessage("Credit card can not be saved.")
                        }
                        alert.show()
                    }
                }

                override fun onFailure(call: Call?, e: IOException?) {
                    println("Can not get tables.")
                }
            })
        }

    }
}
