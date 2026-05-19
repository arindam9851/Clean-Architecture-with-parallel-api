package com.example.newapplication.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CoinApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PokemonApiRetrofit