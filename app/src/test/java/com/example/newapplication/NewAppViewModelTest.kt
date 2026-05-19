package com.example.newapplication

import com.example.newapplication.data.remote.dto.coin.CoinDtoModelItem
import com.example.newapplication.data.remote.dto.pokemon.PokemonResultDto
import com.example.newapplication.domain.model.coin.CoinDomainModel
import com.example.newapplication.domain.model.pokemon.PokemonDomainModel
import com.example.newapplication.domain.usecase.NewAppUseCase
import com.example.newapplication.presentation.intent.NewAppScreenIntent
import com.example.newapplication.presentation.viewmodel.NewAppViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewAppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NewAppViewModel

    private val useCase: NewAppUseCase = mockk()

    @Before
    fun setup() {
        viewModel = NewAppViewModel(useCase)
    }

    @Test
    fun `load all data success`() = runTest {

        val coinList = listOf(
            CoinDomainModel(
                id = "btc-bitcoin",
                name = "Bitcoin",
                is_new = false,
                rank = 1,
                symbol = "aa",
                type = "bb"

            )
        )

        val pokemonList = listOf(
            PokemonDomainModel(
                name = "Pikachu"

            )
        )

        coEvery {
            useCase.getCoinList()
        } returns coinList

        coEvery {
            useCase.getPokemonList()
        } returns pokemonList


        viewModel.handleIntent(
            NewAppScreenIntent.LoadAllData
        )

        advanceUntilIdle()


        val state = viewModel.state.value

        assert(state.isLoading == false)

        assert(state.coinList.isNotEmpty())

        assert(state.pokemonList.isNotEmpty())

        assert(state.coinError == null)
        assert(state.pokemonError == null)
    }

    @Test
    fun `load all data coin api failure`() = runTest {


        coEvery {
            useCase.getCoinList()
        } throws RuntimeException("Coin API Failed")

        coEvery {
            useCase.getPokemonList()
        } returns emptyList()


        viewModel.handleIntent(
            NewAppScreenIntent.LoadAllData
        )

        advanceUntilIdle()


        val state = viewModel.state.value

        assert(state.isLoading == false)

        assert(state.coinList.isEmpty())

        assert(state.pokemonList.isEmpty())

    }
}