package com.example.newapplication.data.remote.api

import com.example.newapplication.data.remote.dto.coin.CoinDtoModelItem
import com.example.newapplication.data.remote.dto.pokemon.PokemonDtoModel
import retrofit2.http.GET

interface PokemonApi {
    @GET("api/v2/pokemon")
    suspend fun getPokemonList() : PokemonDtoModel
}