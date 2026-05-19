package com.example.newapplication.data.repository

import com.example.newapplication.data.mapper.toDomain
import com.example.newapplication.data.remote.api.PokemonApi
import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel
import com.example.newapplication.domain.repository.PokemonRepository
import javax.inject.Inject

class PokemonRepositoryImpl @Inject constructor(
    private val api : PokemonApi
) : PokemonRepository {
    override suspend fun getListOfPokemon(): List<PokemonDomainModel> {
        val response = api.getPokemonList()
        return response.results.map{it.toDomain()}
    }
}