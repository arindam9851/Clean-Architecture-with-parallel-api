package com.example.newapplication.presentation.mapper

import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel
import com.example.newapplication.presentation.model.coin.CoinUIModel
import com.example.newapplication.presentation.model.pokemon.PokemonUIModel

fun CoinDomainModel.toUI(): CoinUIModel{
    return CoinUIModel(
        id=id,
        is_new=is_new,
        name = name,
    )
}

fun PokemonDomainModel.toUI(): PokemonUIModel{
    return PokemonUIModel(
        name = name,
    )
}