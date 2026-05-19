package com.example.newapplication.domain.model.coin

data class CoinDomainModel(
    val id: String,
    val is_new: Boolean,
    val name: String,
    val rank: Int,
    val symbol: String,
    val type: String
)
