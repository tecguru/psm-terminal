package com.stripe.example.model

import io.realm.RealmObject

open class RealmReceipt(
    var customerName: String?= null,
    var transactionAmount: Double = 0.00,
    var transactionId: String?= null,
    var descriptionId: String?= null,
    var cardType: String?= null,
    var cardLastFour: Int?= null,
    var isCompleted: Boolean= false,
    var isFailed: Boolean= false,
    var errorMessage: String? = null
) : RealmObject(){}
