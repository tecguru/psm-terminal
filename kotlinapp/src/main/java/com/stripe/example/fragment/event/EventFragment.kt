package com.stripe.example.fragment.event

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stripe.example.NavigationListener
import com.stripe.example.R
import com.stripe.example.databinding.FragmentEventBinding
import com.stripe.example.model.Event
import com.stripe.example.network.ApiClient
import com.stripe.example.MainActivity
import com.stripe.example.viewmodel.EventViewModel
import com.stripe.stripeterminal.Terminal
import com.stripe.stripeterminal.callable.Callback
import com.stripe.stripeterminal.callable.PaymentIntentCallback
import com.stripe.stripeterminal.callable.PaymentMethodCallback
import com.stripe.stripeterminal.callable.ReaderDisplayListener
import com.stripe.stripeterminal.model.external.PaymentIntent
import com.stripe.stripeterminal.model.external.PaymentIntentParameters
import com.stripe.stripeterminal.model.external.PaymentMethod
import com.stripe.stripeterminal.model.external.ReadReusableCardParameters
import com.stripe.stripeterminal.model.external.ReaderDisplayMessage
import com.stripe.stripeterminal.model.external.ReaderInputOptions
import com.stripe.stripeterminal.model.external.TerminalException
import java.lang.ref.WeakReference
import java.util.Locale
import com.anggastudio.printama.Printama
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event.view.event_recycler_view
import kotlinx.android.synthetic.main.fragment_payment.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt


/**
 * The `EventFragment` displays events as they happen during a payment flow
 */
class EventFragment : Fragment(), ReaderDisplayListener {
    lateinit var printama: Printama
    //var firstRun = false
    companion object {

        const val TAG = "com.stripe.example.fragment.event.EventFragment"

        var new_amount = ""
        var price = ""
        var paymentintent_id = ""
        var paymentintent_status = ""
        private const val AMOUNT = "com.stripe.example.fragment.event.EventFragment.amount"
        private const val CURRENCY = "com.stripe.example.fragment.event.EventFragment.currency"
        private const val REQUEST_PAYMENT = "com.stripe.example.fragment.event.EventFragment.request_payment"
        private const val READ_REUSABLE_CARD = "com.stripe.example.fragment.event.EventFragment.read_reusable_card"

        fun readReusableCard(): EventFragment {
            val fragment = EventFragment()
            val bundle = Bundle()
            bundle.putBoolean(READ_REUSABLE_CARD, true)
            bundle.putBoolean(REQUEST_PAYMENT, false)
            fragment.arguments = bundle
            return fragment
        }

        fun requestPayment(amount: Int, requestPayment:String, currency: String): EventFragment {
            println(requestPayment)
            val fragment = EventFragment()
            val bundle = Bundle()
            price=requestPayment.toString()
            bundle.putInt(AMOUNT, amount)
            bundle.putString(CURRENCY, currency)
            bundle.putBoolean(REQUEST_PAYMENT, true)
            bundle.putBoolean(READ_REUSABLE_CARD, false)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var adapter: EventAdapter
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var activityRef: WeakReference<FragmentActivity?>

    private lateinit var binding: FragmentEventBinding
    private lateinit var viewModel: EventViewModel

    private var paymentIntent: PaymentIntent? = null



    private val processPaymentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Processed payment", "terminal.processPayment")
                ApiClient.capturePaymentIntent(paymentIntent.id)
                addEvent("Captured PaymentIntent", "backend.capturePaymentIntent")
                completeFlow()

                //print code here
              //  var newwprice = convertAmount(price)
             //  var newwprice = price.toInt()/100.toDouble()
                paymentintent_id = paymentIntent.id
                paymentintent_status= paymentIntent.confirmationMethod.toString() + paymentIntent.application + paymentIntent.created +
                        paymentIntent.statementDescriptor + paymentIntent.id

                //new_amount =  roundOffDecimal(newwprice)

                val number:Double = price.toDouble()
                val number4digits:Double = String.format("%.4f", number).toDouble()
                val number3digits:Double = String.format("%.3f", number4digits).toDouble()
                val solution:Double = String.format("%.2f", number3digits).toDouble()

                new_amount = solution.toString()

                Btn_reprint.visibility = View.VISIBLE
                try{
                    Toast.makeText(activity, new_amount, Toast.LENGTH_LONG)
                        .show()
                    printReceipt(
                        tid = paymentIntent.id,
                        amount = new_amount,
                        currency = "USD",
                        status = "succeeded"
                    )
                } catch (e: Exception) {
                    Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG)
                        .show()
                }


            }


            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private fun convertAmount(amount: String): String {
        val hundred = BigDecimal(100)
        val rsult = amount.toBigDecimal().multiply(hundred).stripTrailingZeros().toPlainString()
        println("amountBigDecimal = $rsult")

        return (amount.toBigDecimal().multiply(hundred).stripTrailingZeros()).toPlainString().toString()
    }

    private val cancelPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Canceled PaymentIntent", "terminal.cancelPaymentIntent")
                activityRef.get()?.let {
                    if (it is NavigationListener) {
                        it.runOnUiThread {
                            it.onCancelCollectPaymentMethod()
                        }
                    }
                }
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val collectPaymentMethodCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                addEvent("Collected PaymentMethod", "terminal.collectPaymentMethod")
                Terminal.getInstance().processPayment(paymentIntent, processPaymentCallback)
                viewModel.collectTask = null
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val createPaymentIntentCallback by lazy {
        object : PaymentIntentCallback {
            override fun onSuccess(paymentIntent: PaymentIntent) {
                this@EventFragment.paymentIntent = paymentIntent
                addEvent("Created PaymentIntent", "terminal.createPaymentIntent")
                viewModel.collectTask = Terminal.getInstance().collectPaymentMethod(
                        paymentIntent, this@EventFragment, collectPaymentMethodCallback)
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    private val reusablePaymentMethodCallback by lazy {
        object : PaymentMethodCallback {
            override fun onSuccess(paymentMethod: PaymentMethod) {
                addEvent("Created PaymentMethod: ${paymentMethod.id}", "terminal.readReusableCard")
                Btn_reprint.visibility = View.VISIBLE
                completeFlow()
            }

            override fun onFailure(e: TerminalException) {
                this@EventFragment.onFailure(e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRef = WeakReference(activity)
        viewModel = ViewModelProviders.of(this).get(EventViewModel::class.java)
        adapter = EventAdapter(viewModel)


        val sharedPreference : SharedPreferences?= activity?.getPreferences(Context.MODE_PRIVATE);

        if(sharedPreference?.getString("printer","defaultName").toString() != "true") {
            sharedPreference?.edit()?.putString("printer", "false")?.apply()
        }





            //print

        //get value of global var used getApplication
        var mApp = MainActivity()
        var strGlobalVar = mApp.firstRun


        if (sharedPreference?.getString("printer","defaultName").toString().equals("falsee"))
        {
            Printama.showPrinterList(activity) { printerName ->
                sharedPreference?.edit()?.putString("printer", "truee")?.apply()
            }
        }



        printama = Printama(activity)

        if (savedInstanceState == null) {
            arguments?.let {
                if (it.getBoolean(REQUEST_PAYMENT)) {
                    val params = PaymentIntentParameters.Builder()
                            .setAmount(it.getInt(AMOUNT))
                            .setCurrency(it.getString(CURRENCY)?.toLowerCase(Locale.ENGLISH) ?: "usd")
                            .build()
                    Terminal.getInstance().createPaymentIntent(params, createPaymentIntentCallback)
                } else if (it.getBoolean(READ_REUSABLE_CARD)) {
                    viewModel.collectTask = Terminal.getInstance().readReusableCard(
                            ReadReusableCardParameters.NULL, this@EventFragment, reusablePaymentMethodCallback)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        eventRecyclerView = binding.root.event_recycler_view
        eventRecyclerView.layoutManager = LinearLayoutManager(activity)
        eventRecyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Btn_reprint.visibility = View.GONE
        cancel_button.setOnClickListener {
            viewModel.collectTask?.cancel(object : Callback {
                override fun onSuccess() {
                    viewModel.collectTask = null
                    paymentIntent?.let {
                        Terminal.getInstance().cancelPaymentIntent(it, cancelPaymentIntentCallback)
                    }
                }

                override fun onFailure(e: TerminalException) {
                    viewModel.collectTask = null
                    this@EventFragment.onFailure(e)
                }
            })
        }

        done_button.setOnClickListener {
            activityRef.get()?.let {
                if (it is NavigationListener) {
                    it.runOnUiThread {
                        it.onRequestExitWorkflow()
                    }
                }
            }
        }
        Btn_reprint.setOnClickListener{
          //  Toast.makeText(activity, new_amount, Toast.LENGTH_LONG)
           //     .show()
            try{
                printReceipt(
                    tid = paymentintent_id.toString(),
                    amount = new_amount.toString(),
                    currency = "USD",
                    status = "succeeded"
                )
            } catch (e: Exception) {
                Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onRequestReaderDisplayMessage(message: ReaderDisplayMessage) {
        addEvent(message.toString(), "listener.onRequestReaderDisplayMessage")
    }

    override fun onRequestReaderInput(options: ReaderInputOptions) {
        addEvent(options.toString(), "listener.onRequestReaderInput")
    }

    fun completeFlow() {
        activityRef.get()?.let {
            it.runOnUiThread {
                viewModel.isComplete.value = true
            }
        }
    }

    fun addEvent(message: String, method: String) {
        activityRef.get()?.let { activity ->
            activity.runOnUiThread {
                viewModel.addEvent(Event(message, method))
            }
        }
    }

    private fun onFailure(e: TerminalException) {
        addEvent(e.errorMessage, e.errorCode.toString())
        completeFlow()
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
