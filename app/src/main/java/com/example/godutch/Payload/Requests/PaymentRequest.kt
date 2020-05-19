package com.example.godutch.Payload.Requests

data class PaymentRequest (
    var restaurantId : String,
    var username : String,
    var tableName : String,
    var price : Double,
    var cardAlias : String
)