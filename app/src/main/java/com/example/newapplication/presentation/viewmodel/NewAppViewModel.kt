package com.example.newapplication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newapplication.domain.usecase.NewAppUseCase
import com.example.newapplication.presentation.intent.NewAppScreenIntent
import com.example.newapplication.presentation.mapper.toUI
import com.example.newapplication.presentation.state.NewAppScreenSate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewAppViewModel @Inject constructor(
    private val useCase: NewAppUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NewAppScreenSate())
    val state: StateFlow<NewAppScreenSate> = _state


    fun handleIntent(intent: NewAppScreenIntent) {
        viewModelScope.launch {
            when (intent) {
                is NewAppScreenIntent.LoadAllData -> {

                    // Loading
                    _state.value = _state.value.copy(
                        isLoading = true,
                        coinError = null,
                        pokemonError = null
                    )
                    val coinDeferred = async {
                        runCatching {
                            useCase.getCoinList()
                        }
                    }

                    val pokemonDeferred = async {
                        runCatching {
                            useCase.getPokemonList()
                        }
                    }

                    val coinResult = coinDeferred.await()
                    val pokemonResult = pokemonDeferred.await()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        coinList = coinResult.getOrNull()
                            ?.map { it.toUI() }
                            ?: emptyList(),
                        pokemonList = pokemonResult.getOrNull()
                            ?.map { it.toUI() }
                            ?: emptyList(),
                        coinError = coinResult.exceptionOrNull()?.message,
                        pokemonError = pokemonResult.exceptionOrNull()?.message
                    )
                }

            }
        }

    }

}