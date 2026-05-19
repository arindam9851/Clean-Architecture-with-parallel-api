package com.example.newapplication.domain.repository

import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel

interface PokemonRepository {
    suspend fun getListOfPokemon(): List<PokemonDomainModel>
}