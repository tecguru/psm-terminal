package com.stripe.example.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stripe.example.ManualpaymentActivity
import com.stripe.example.NavigationListener
import com.stripe.example.R
import kotlinx.android.synthetic.main.fragment_payment.view.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

//import kotlinx.android.synthetic.main.fragment_payment.view.currency_edit_text

/**
 * The `PaymentFragment` allows the user to create a custom payment and ask the reader to handle it.
 */
class PaymentFragment : Fragment() {

    companion object {
        const val TAG = "com.stripe.example.fragment.PaymentFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_payment, container, false)

        view.amount_edit_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable.toString().isNotEmpty()) {
                    view.charge_amount.text = view.amount_edit_text.text.toString() //formatCentsToString(editable.toString().toInt())
                }
            }
        })

        view.collect_payment_button.setOnClickListener {
            if (activity is NavigationListener) {
                if(view.amount_edit_text.text.toString() == "0" || view.amount_edit_text.text.toString() == "")
                {
                    Toast.makeText(
                        activity,
                        "Amount Invalid",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
//                    val amountCon = view.amount_edit_text.text.toString().toDouble() * 100
                    (activity as NavigationListener).onRequestPayment(
                        convertAmount(view.amount_edit_text.text.toString()),
                        view.amount_edit_text.text.toString(),
                        "usd"
                    )
                }
            }
        }

        view.home_button.setOnClickListener {
            if (activity is NavigationListener) {
                (activity as NavigationListener).onRequestExitWorkflow()
            }
        }

        view.manual_mode.setOnClickListener {
            val intent = Intent(context, ManualpaymentActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun formatCentsToString(i: Int): String {
        return NumberFormat.getCurrencyInstance(Locale.US).format(i / 100.0)
    }

    private fun convertAmount(amount: String): Int {
        val value = BigDecimal(amount)
        val hundred = BigDecimal.valueOf(100)
        val result = value.multiply(hundred).stripTrailingZeros().toPlainString()
        return result.toInt()
    }

}
