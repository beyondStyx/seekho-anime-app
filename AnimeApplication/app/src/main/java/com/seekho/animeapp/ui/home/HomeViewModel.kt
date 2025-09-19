package com.seekho.animeapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seekho.animeapp.data.model.AnimeItem
import com.seekho.animeapp.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<AnimeItem> = emptyList(),
    val error: String? = null,
    val canLoadMore: Boolean = false,
    val currentPage: Int = 1
)

class HomeViewModel(
    private val repo: AnimeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state

    init { load(page = 1) }

    fun load(page: Int, limit: Int = 24) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val res = repo.getTopAnime(page, limit, sfw = true)
            if (res.isSuccess) {
                val response = res.getOrNull()!!        // TopAnimeResponse
                val pageItems = response.data.orEmpty() // List<AnimeItem>
                val merged = if (page == 1) pageItems else _state.value.items + pageItems

                _state.update {
                    it.copy(
                        isLoading = false,
                        items = merged.distinctBy { it.malId },
                        error = null,
                        canLoadMore = response.pagination?.hasNextPage == true,
                        currentPage = page
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = res.exceptionOrNull()?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        val st = _state.value
        if (!st.isLoading && st.canLoadMore) load(st.currentPage + 1)
    }

    companion object {
        fun provide(
            owner: androidx.lifecycle.ViewModelStoreOwner,
            appContext: android.content.Context
        ): HomeViewModel {
            return ViewModelProvider(owner, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(AnimeRepository(appContext)) as T
                }
            })[HomeViewModel::class.java]
        }
    }
}