package com.example.newapplication.domain.repository

import com.example.newapplication.domain.model.coin.CoinDomainModel

interface CoinRepository {
    suspend fun getListOfCoins(): List<CoinDomainModel>

}