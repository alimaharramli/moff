package com.example.learningble.dto

import java.math.BigDecimal

data class TransactionDto (
    val amount: BigDecimal,
    val date: Long,
    val from: String,
    val to: String
)