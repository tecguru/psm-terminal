package com.stripe.example.model

data class PaymentResponseClass(
    val amount: Int, // 2000
    val amount_captured: Int, // 2000
    val amount_refunded: Int, // 0
    val application: Any, // null
    val application_fee: Any, // null
    val application_fee_amount: Any, // null
    val balance_transaction: String, // txn_1ItrhW2eZvKYlo2CYeSKC0Yr
    val billing_details: BillingDetails,
    val calculated_statement_descriptor: String, // STRIPE.COM
    val captured: Boolean, // true
    val created: Int, // 1621677897
    val currency: String, // usd
    val customer: Any, // null
    val description: String, // My First Test Charge (created for API docs)
    val destination: Any, // null
    val dispute: Any, // null
    val disputed: Boolean, // false
    val failure_code: Any, // null
    val failure_message: Any, // null
    val fraud_details: FraudDetails,
    val id: String, // ch_1ItrhV2eZvKYlo2C5dfQDAe7
    val invoice: Any, // null
    val livemode: Boolean, // false
    val metadata: Metadata,
    val `object`: String, // charge
    val on_behalf_of: Any, // null
    val order: Any, // null
    val outcome: Outcome,
    val paid: Boolean, // true
    val payment_intent: Any, // null
    val payment_method: String, // card_1Itra02eZvKYlo2C5xxbHVIE
    val payment_method_details: PaymentMethodDetails,
    val receipt_email: Any, // null
    val receipt_number: Any, // null
    val receipt_url: String, // https://pay.stripe.com/receipts/acct_1032D82eZvKYlo2C/ch_1ItrhV2eZvKYlo2C5dfQDAe7/rcpt_JWvYhv2dW3Uq6SbKjnhqF7eP6XwEcbO
    val refunded: Boolean, // false
    val refunds: Refunds,
    val review: Any, // null
    val shipping: Any, // null
    val source: Source,
    val source_transfer: Any, // null
    val statement_descriptor: Any, // null
    val statement_descriptor_suffix: Any, // null
    val status: String, // succeeded
    val transfer_data: Any, // null
    val transfer_group: Any // null
) {
    data class BillingDetails(
        val address: Address,
        val email: Any, // null
        val name: Any, // null
        val phone: Any // null
    ) {
        data class Address(
            val city: Any, // null
            val country: Any, // null
            val line1: Any, // null
            val line2: Any, // null
            val postal_code: Any, // null
            val state: Any // null
        )
    }

    class FraudDetails(
    )

    class Metadata(
    )

    data class Outcome(
        val network_status: String, // approved_by_network
        val reason: Any, // null
        val risk_level: String, // normal
        val risk_score: Int, // 10
        val seller_message: String, // Payment complete.
        val type: String // authorized
    )

    data class PaymentMethodDetails(
        val card: Card,
        val type: String // card
    ) {
        data class Card(
            val brand: String, // visa
            val checks: Checks,
            val country: String, // US
            val exp_month: Int, // 5
            val exp_year: Int, // 2022
            val fingerprint: String, // Xt5EWLLDS7FJjR1c
            val funding: String, // credit
            val installments: Any, // null
            val last4: String, // 4242
            val network: String, // visa
            val three_d_secure: Any, // null
            val wallet: Any // null
        ) {
            data class Checks(
                val address_line1_check: Any, // null
                val address_postal_code_check: Any, // null
                val cvc_check: String // pass
            )
        }
    }

    data class Refunds(
        val `data`: List<Any>,
        val has_more: Boolean, // false
        val `object`: String, // list
        val total_count: Int, // 0
        val url: String // /v1/charges/ch_1ItrhV2eZvKYlo2C5dfQDAe7/refunds
    )

    data class Source(
        val address_city: Any, // null
        val address_country: Any, // null
        val address_line1: Any, // null
        val address_line1_check: Any, // null
        val address_line2: Any, // null
        val address_state: Any, // null
        val address_zip: Any, // null
        val address_zip_check: Any, // null
        val brand: String, // Visa
        val country: String, // US
        val customer: Any, // null
        val cvc_check: String, // pass
        val dynamic_last4: Any, // null
        val exp_month: Int, // 5
        val exp_year: Int, // 2022
        val fingerprint: String, // Xt5EWLLDS7FJjR1c
        val funding: String, // credit
        val id: String, // card_1Itra02eZvKYlo2C5xxbHVIE
        val last4: String, // 4242
        val metadata: Metadata,
        val name: Any, // null
        val `object`: String, // card
        val tokenization_method: Any // null
    ) {
        class Metadata(
        )
    }
}