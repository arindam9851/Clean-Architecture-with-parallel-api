package com.example.newapplication.data.repository

import com.example.newapplication.data.mapper.toDomain
import com.example.newapplication.data.remote.api.CoinApi
import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.repository.CoinRepository
import javax.inject.Inject

class CoinRepositoryImpl @Inject constructor(
    private val api : CoinApi
) : CoinRepository {
    override suspend fun getListOfCoins(): List<CoinDomainModel> {
        return api.getCoinList().map{it.toDomain()}
    }
}