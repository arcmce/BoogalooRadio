package com.arcmce.boogaloo.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arcmce.boogaloo.R
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*
import com.stripe.android.ApiResultCallback
import com.stripe.android.GooglePayConfig
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.android.synthetic.main.controls_layout.view.*
import kotlinx.android.synthetic.main.ordering.view.*
import org.json.JSONArray
import org.json.JSONObject


class OrderingFragment : Fragment() {

//    private val stripe = StripeFactory(application).create()

    private val stripe: Stripe by lazy {
        Stripe(
            activity!!,
            "pk_test_51HCYLuJFKK2qf5cSZY7tnbWmF0wiCC1fgRkXrRw7ncu4oBoXFudaec4uTcL7NKdPCkjkxEttr4BNxhXeICav06XQ008ol8v3w0",
            null,
            true
        )
    }


    private val paymentsClient: PaymentsClient by lazy {
        Wallet.getPaymentsClient(
            activity!!,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build()
        )
    }

    private fun isReadyToPay() {
        paymentsClient.isReadyToPay(createIsReadyToPayRequest())
            .addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        Log.d("ORF", "isReadyToPay successful")
                        // show Google Pay as payment option
                    } else {
                        Log.d("ORF", "isReadyToPay unsuccessful")
                        // hide Google Pay as payment option
                    }
                } catch (exception: ApiException) {
                }
            }

//        request?.let {
//            val task = paymentsClient?.isReadyToPay(request)
//            task?.addOnCompleteListener { completedTask ->
//                val result = completedTask.getResult(ApiException::class.java)
//                result?.let {
//                    button_google_pay.setOnClickListener { view -> requestPayment(view) }
//                    button_google_pay.visibility = View.VISIBLE
//                }
//            }
//        }

    }

    private fun createIsReadyToPayRequest(): IsReadyToPayRequest {
        return IsReadyToPayRequest.fromJson(
            JSONObject()
                .put("allowedAuthMethods", JSONArray()
                    .put("PAN_ONLY")
                    .put("CRYPTOGRAM_3DS")
                )
                .put("allowedCardNetworks",
                    JSONArray()
                        .put("AMEX")
                        .put("DISCOVER")
                        .put("MASTERCARD")
                        .put("VISA")
                )
                .toString()
        )
    }

    private fun createPaymentDataRequest(): PaymentDataRequest {
        // create PaymentMethod
        val gatewayInformation = JSONObject().apply {
            put("gateway", "stripe")
            put("stripe:version", "2018-10-31")
            put("stripe:publishableKey", "pk_test_51HCYLuJFKK2qf5cSZY7tnbWmF0wiCC1fgRkXrRw7ncu4oBoXFudaec4uTcL7NKdPCkjkxEttr4BNxhXeICav06XQ008ol8v3w0")
        }

        val tokenizationSpecification = JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", gatewayInformation)
        }

        val cardPaymentMethod = JSONObject()
            .put("type", "CARD")
            .put(
                "parameters",
                JSONObject()
                    .put("allowedAuthMethods", JSONArray()
                        .put("PAN_ONLY")
                        .put("CRYPTOGRAM_3DS"))
                    .put("allowedCardNetworks",
                        JSONArray()
                            .put("AMEX")
                            .put("DISCOVER")
                            .put("MASTERCARD")
                            .put("VISA"))

                    // require billing address
                    .put("billingAddressRequired", true)
                    .put(
                        "billingAddressParameters",
                        JSONObject()
                            // require full billing address
                            .put("format", "MIN")

                            // require phone number
                            .put("phoneNumberRequired", true)
                    )
            )
            .put(
                "tokenizationSpecification",
                tokenizationSpecification
            )

        // create PaymentDataRequest
        val paymentDataRequest = JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)
            .put("allowedPaymentMethods",
                JSONArray().put(cardPaymentMethod))
            .put("transactionInfo", JSONObject()
                .put("totalPrice", "10.00")
                .put("totalPriceStatus", "FINAL")
                .put("currencyCode", "GBP")
            )
            .put("merchantInfo", JSONObject()
                .put("merchantName", "Boogaloo"))

            // require email address
            .put("emailRequired", true)
            .toString()

        return PaymentDataRequest.fromJson(paymentDataRequest)
    }

    private fun payWithGoogle() {
        AutoResolveHelper.resolveTask(
            paymentsClient.loadPaymentData(createPaymentDataRequest()),
            activity!!,
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 53
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data != null) {
                            onGooglePayResult(data)
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        // Cancelled
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        // Log the status for debugging
                        // Generally there is no need to show an error to
                        // the user as the Google Payment API will do that
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                    }
                    else -> {
                        // Do nothing.
                    }
                }
            }
            else -> {
                // Handle any other startActivityForResult calls you may have made.
            }
        }
    }

    private fun onGooglePayResult(data: Intent) {
        val paymentData = PaymentData.getFromIntent(data) ?: return
        val paymentMethodCreateParams =
            PaymentMethodCreateParams.createFromGooglePay(
                JSONObject(paymentData.toJson())
            )

        // now use the `paymentMethodCreateParams` object to create a PaymentMethod
        stripe.createPaymentMethod(
            paymentMethodCreateParams,
            callback = object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                    Log.d("ORF", "createPaymentMethod success")
                }

                override fun onError(e: Exception) {
                    Log.d("ORF", "createPaymentMethod error")
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.ordering, container, false)

        view.buyButton.setOnClickListener {
            buyButtonClick()
        }

        view.gPayButton.setOnClickListener {
//            payWithGoogle()
        }

        return view

    }

    fun buyButtonClick() {
        Log.d("ORF", "playButtonClick")
        isReadyToPay()
    }
}