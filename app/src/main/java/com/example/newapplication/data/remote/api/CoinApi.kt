package com.example.newapplication.data.remote.api

import com.example.newapplication.data.remote.dto.coin.CoinDtoModelItem
import retrofit2.http.GET

interface CoinApi {
    @GET("v1/coins")
    suspend fun getCoinList() : List<CoinDtoModelItem>
}