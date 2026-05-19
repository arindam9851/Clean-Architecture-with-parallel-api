package com.example.newapplication.data.remote.dto.pokemon

data class PokemonDtoModel(
    val count: Int,
    val next: String,
    val previous: Any,
    val results: List<PokemonResultDto>
)