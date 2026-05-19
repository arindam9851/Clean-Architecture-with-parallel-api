package com.example.newapplication.di

import com.example.newapplication.data.remote.api.CoinApi
import com.example.newapplication.data.repository.CoinRepositoryImpl
import com.example.newapplication.domain.repository.CoinRepository
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
object CoinAppModule {

    @Provides
    @Singleton
    @Named("coinRetrofit")
    fun provideRetrofit(): Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://api.coinpaprika.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    @Provides
    @Singleton
    fun provideApi( @Named("coinRetrofit") retrofit: Retrofit) : CoinApi{
        return retrofit.create(CoinApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(coinApi: CoinApi) : CoinRepository{
        return CoinRepositoryImpl(coinApi)
    }
}