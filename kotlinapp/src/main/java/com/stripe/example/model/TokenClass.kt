package com.stripe.example.model

data class TokenClass(
    val card: Card,
    val client_ip: String, // 103.26.85.243
    val created: Int, // 1621679300
    val id: String, // tok_1Its482eZvKYlo2CGBop3yPZ
    val livemode: Boolean, // false
    val `object`: String, // token
    val type: String, // card
    val used: Boolean // false
) {
    data class Card(
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
        val cvc_check: String, // unchecked
        val dynamic_last4: Any, // null
        val exp_month: Int, // 5
        val exp_year: Int, // 2022
        val fingerprint: String, // Xt5EWLLDS7FJjR1c
        val funding: String, // credit
        val id: String, // card_1Its482eZvKYlo2CbQNg9mGi
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