package com.example.newapplication.data.mapper

import com.example.newapplication.data.remote.dto.coin.CoinDtoModelItem
import com.example.newapplication.data.remote.dto.pokemon.PokemonResultDto
import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel


fun CoinDtoModelItem.toDomain(): CoinDomainModel{
    return CoinDomainModel(
        id=id,
        is_new=is_new,
        name = name,
        rank= rank,
        symbol=symbol,
        type=type
    )
}

fun PokemonResultDto.toDomain(): PokemonDomainModel{
    return PokemonDomainModel(
        name=name
    )
}