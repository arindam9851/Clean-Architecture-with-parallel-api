package com.example.newapplication.di

import com.example.newapplication.data.remote.api.CoinApi
import com.example.newapplication.data.remote.api.PokemonApi
import com.example.newapplication.data.repository.CoinRepositoryImpl
import com.example.newapplication.data.repository.PokemonRepositoryImpl
import com.example.newapplication.domain.repository.CoinRepository
import com.example.newapplication.domain.repository.PokemonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PokemonAppModule {
    @Provides
    @Singleton
    @Named("pokemonRetrofit")
    fun provideRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://pokeapi.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    @Provides
    @Singleton
    fun provideApi(@Named("pokemonRetrofit") retrofit: Retrofit) : PokemonApi{
        return retrofit.create(PokemonApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(pokemonApi: PokemonApi) : PokemonRepository{
        return PokemonRepositoryImpl(pokemonApi)
    }
}