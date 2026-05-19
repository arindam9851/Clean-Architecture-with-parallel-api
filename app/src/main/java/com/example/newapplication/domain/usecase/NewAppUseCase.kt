package com.example.newapplication.domain.usecase

import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel
import com.example.newapplication.domain.repository.CoinRepository
import com.example.newapplication.domain.repository.PokemonRepository
import javax.inject.Inject

class NewAppUseCase @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val coinRepository : CoinRepository
){
    suspend fun getCoinList(): List<CoinDomainModel>{
        return coinRepository.getListOfCoins()
    }

    suspend fun getPokemonList(): List<PokemonDomainModel>{
        return pokemonRepository.getListOfPokemon()
    }
}