package com.example.learningble.repo

import com.example.learningble.dto.TransactionDto
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface TransactionRepo {

    @POST("transactions")
    fun makeTransaction(@Body transaction: TransactionDto): Call<Void>

}