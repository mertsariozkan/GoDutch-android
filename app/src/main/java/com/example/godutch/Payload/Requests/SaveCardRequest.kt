package com.example.godutch.Payload.Requests

data class SaveCardRequest (
    var cardAlias: String,
    var cardHolderName: String,
    var cardNumber: String,
    var expireMonth: String,
    var expireYear: String,
    var email: String,
    var ownerId: String
)