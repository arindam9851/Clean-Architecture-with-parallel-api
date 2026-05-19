package com.example.newapplication.presentation.state

import com.example.newapplication.presentation.model.coin.CoinUIModel
import com.example.newapplication.presentation.model.pokemon.PokemonUIModel

data class NewAppScreenSate(
    val isLoading : Boolean = false,
    val coinList : List<CoinUIModel> = emptyList(),
    val pokemonList : List<PokemonUIModel> = emptyList(),
    val coinError: String? = null,
    val pokemonError: String? = null
)
