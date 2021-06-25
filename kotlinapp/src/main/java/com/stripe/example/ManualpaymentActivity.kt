package com.stripe.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import com.anggastudio.printama.Printama
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.stripe.example.fragment.TerminalFragment
import com.stripe.example.fragment.event.EventFragment
import com.stripe.example.model.PaymentResponseClass
import com.stripe.example.model.TokenClass
import com.stripe.example.viewmodel.TerminalViewModel
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manualpayment.*
import kotlinx.android.synthetic.main.activity_selection.*
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_terminal.*
import org.json.JSONObject
import java.math.BigDecimal

class ManualpaymentActivity : AppCompatActivity(){
    var receivedToken = MutableLiveData<Event<String>>()
    var showCardOptions = MutableLiveData<Event<Boolean>>()
    lateinit var printama: Printama
    var firstRun = false
    lateinit var client: AsyncHttpClient
    //private lateinit var viewModel: TerminalViewModel
    var tid_globe = ""
     var amount_globe = ""
    companion object {
        const val TAG = "com.stripe.example.ManulapaymentActivity"
        private const val PRINTED_SWITCH = "printed"
        val url = "https://api.stripe.com/v1/tokens"
        val chargesURl = "https://api.stripe.com/v1/charges"
        val apikey = "sk_test_cSY0MigPkp1cJJWnHrmZq0YY"
        var amount = ""
        var token = ""
        var arePermissionsGranted = false
        lateinit var tokenClass: TokenClass
        lateinit var paymentResponseClass: PaymentResponseClass
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manualpayment)
        btn_reprint.visibility = View.GONE
        goback.setOnClickListener {
            val intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
        }



        val sharedPreference =  getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        if (!sharedPreference.getString("printer","defaultName").toString().equals("truee"))
        {
            editor.putString("printer","falsee")
            editor.apply()
        }

        client = AsyncHttpClient()
        client.setBasicAuth(apikey, "")
        checkPermissions()

        //get value of global var used getApplication
        val mApp = MainActivity()
        var strGlobalVar = mApp.firstRun





        //viewModel = TerminalViewModel(true,false)



        if (sharedPreference.getString("printer","defaultName").toString().equals("falsee")) {

            Printama.showPrinterList(this) { printerName ->
                strGlobalVar = true
               /* val isPrinted = this?.getSharedPreferences(
                    ManualpaymentActivity.TAG,
                    Context.MODE_PRIVATE)?.getBoolean(ManualpaymentActivity.PRINTED_SWITCH, true) ?: true
                viewModel = TerminalViewModel(true,isPrinted) */
                editor.putString("printer","truee")
                editor.apply()

            }

        }

        printama = Printama(this)

        receivedToken.observe(this) { event ->
            event.getContentIfNotHandled().let { token1 ->
                client.post(
                    this,
                    chargesURl,
                    createPaymentRequestParams(token1!!, amount),
                    object : JsonHttpResponseHandler() {
                        override fun onSuccess(
                            statusCode: Int,
                            headers: Array<out Header>?,
                            response: JSONObject?
                        ) {
                            progress_bar.visibility = View.GONE
                            paymentResponseClass = Gson().fromJson(
                                response.toString(),
                                PaymentResponseClass::class.java
                            )

                            Toast.makeText(
                                applicationContext,
                                "Transaction Successful",
                                Toast.LENGTH_LONG
                            ).show()

                            val number:Double = et_amount.text.toString().toDouble()
                            val number4digits:Double = String.format("%.4f", number).toDouble()
                            val number3digits:Double = String.format("%.3f", number4digits).toDouble()
                            val solution:Double = String.format("%.2f", number3digits).toDouble()

                            try {
                                tid_globe=paymentResponseClass.balance_transaction
                                amount_globe = "$$solution"
                                printReceipt(
                                    tid = paymentResponseClass.balance_transaction,
                                    amount = "$$solution",
                                    currency = paymentResponseClass.currency,
                                    status = paymentResponseClass.status
                                )
                                clearInputs()
                                if(paymentResponseClass.status == "succeeded")
                                {
                                    btn_reprint.visibility = View.VISIBLE
                                }

                            } catch (e: Exception) {
                                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG)
                                    .show()
                            }
                        }

                        override fun onFailure(
                            statusCode: Int,
                            headers: Array<out Header>?,
                            throwable: Throwable?,
                            errorResponse: JSONObject?
                        ) {
                            progress_bar.visibility = View.GONE
                            println("Charges Response: $errorResponse")
                            Toast.makeText(
                                applicationContext,
                                "Transaction Failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }

        btn_pay.setOnClickListener {
            if (validateInput()) {
                amount = convertAmount(et_amount.text.toString())
                progress_bar.visibility = View.VISIBLE
                println(amount)
                getToken(et_card_Number.text.toString())
            } else {
                Toast.makeText(
                    applicationContext,
                    "All Fields Are required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        btn_reprint.setOnClickListener{
            try {
                // tid_globe=paymentResponseClass.balance_transaction
               // Toast.makeText(applicationContext, tid_globe + amount_globe.toString(), Toast.LENGTH_LONG)
               //     .show()


                printReceipt(
                    tid =  tid_globe.toString(), //paymentResponseClass.balance_transaction,
                    amount = amount_globe.toString(),
                    currency = paymentResponseClass.currency,
                    status = paymentResponseClass.status
                )
                clearInputs()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }


    }

    private fun convertAmount(amount: String): String {
        val hundred = BigDecimal(100)
        val rsult = amount.toBigDecimal().multiply(hundred).stripTrailingZeros().toPlainString()
        println("amountBigDecimal = $rsult")

        return (amount.toBigDecimal().multiply(hundred).stripTrailingZeros()).toPlainString().toString()
    }
    private fun createPaymentRequestParams(token: String, amount: String): RequestParams {
        val params = RequestParams()
        params.put("amount", amount)
        params.put("currency", "usd")
        params.put("source", token)
        params.put("description", "Stripe Demo Payment")
        return params
    }

    private fun createTokenParams(card: String, month: String, year: String, cvc: String): RequestParams {
        val params = RequestParams()
        params.put("card[number]", card)
        params.put("card[exp_month]", month)
        params.put("card[exp_year]", year)
        params.put("card[cvc]", cvc)
        return params
    }

    /*fun getToken(card: String): String {
        val r = post(
            url,
            auth = BasicAuthorization(apikey, ""),
            params = createTokenParams(card),
            headers = mapOf("Content-Type" to "application/x-www-form-urlencoded")
        )
        tokenClass = Gson().fromJson(r.jsonObject.toString(), TokenClass::class.java)
        println("Token response: ${r.text}")
        return tokenClass.id
    }*/

    private fun getToken(card: String) {
        client.post(
            this,
            url,
            createTokenParams(
                card,
                month = et_expiry_month.text.toString(),
                year = et_expiry_year.text.toString(),
                cvc = et_cvc.text.toString()
            ),
            object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    response: JSONObject?
                ) {
                    tokenClass = Gson().fromJson(response.toString(), TokenClass::class.java)
                    receivedToken.postValue(Event(tokenClass.id))
                    token = tokenClass.id
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<out Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    progress_bar.visibility = View.GONE
                    Toast.makeText(applicationContext, errorResponse.toString(), Toast.LENGTH_LONG)
                        .show()
                    println("Failure Response: $errorResponse")
                }
            })

    }

    private fun openBluetoothActivity() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Printer is not Connected")
        builder.setMessage("Do You Want to Open Settings!")
        builder.setPositiveButton("Ok") { x, y ->
            val bluetoothPicker = Intent("android.bluetooth.devicepicker.action.LAUNCH")
            startActivity(bluetoothPicker)
        }
        builder.setNegativeButton("Cancel") { x, y ->
            x.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun checkPermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.INTERNET,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {

                    if (report.areAllPermissionsGranted()) {

                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        arePermissionsGranted = true
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    fun clearInputs(){
        et_card_Number.text.clear()
        et_amount.text.clear()
        et_cvc.text.clear()
        et_expiry_month.text.clear()
        et_expiry_year.text.clear()
    }
    private fun validateInput(): Boolean {
        var result = false
        when {
            (TextUtils.isEmpty(et_card_Number.text.toString().trim())) -> {
                result = false
            }
            (TextUtils.isEmpty(et_amount.text.toString().trim())) -> {
                result = false
            }
            (TextUtils.isEmpty(et_cvc.text.toString().trim())) -> {
                result = false
            }
            (TextUtils.isEmpty(et_expiry_month.text.toString().trim())) -> {
                result = false
            }
            (TextUtils.isEmpty(et_expiry_year.text.toString().trim())) -> {
                result = false
            }
            else -> {
                result = true
            }
        }
        return result
    }


    //printing method
    fun printReceipt(tid: String, amount: String, currency: String, status: String) {
        printama.connect { prin: Printama ->
            printama.setNormalText()
            printama.printTextln(Printama.LEFT, "Perry Stone Ministries")
            printama.printTextln(Printama.LEFT, "3959 Michigan Ave Rd NE")
            printama.printTextln(Printama.LEFT, "Cleveland, TN 37323")
            printama.printTextln(Printama.LEFT, "423-478-3456")
            printama.printDashedLine()
            printama.printTextJustify("TID:", tid)
            printama.printTextJustifyBold("Amount:", amount)
            printama.printTextJustify("Status:", status)
            printama.printDashedLine()
            printama.printTextln(Printama.CENTER, "https://perrystone.org")
            printama.feedPaper()
            printama.close()
        }
    }

}