package com.seekho.animeapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seekho.animeapp.data.model.AnimeCharacter
import com.seekho.animeapp.data.model.AnimeDetail
import com.seekho.animeapp.repository.AnimeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoadingDetails: Boolean = false,
    val isLoadingCast: Boolean = false,
    val detail: AnimeDetail? = null,
    val characters: List<AnimeCharacter> = emptyList(),
    val error: String? = null
)

class DetailViewModel(
    private val repo: AnimeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state

    fun load(id: Int) = viewModelScope.launch {
        _state.update { it.copy(isLoadingDetails = true, isLoadingCast = true, error = null) }

        // Kick both off in parallel
        val detailsDeferred = async { repo.getAnimeDetails(id) }
        val castDeferred    = async { repo.getAnimeCharacters(id) }

        //  DETAILS
        val detailsRes = detailsDeferred.await()
        if (detailsRes.isSuccess) {
            val response = detailsRes.getOrNull()!!      // AnimeDetailsResponse
            _state.update {
                it.copy(detail = response.data, isLoadingDetails = false)
            }
        } else {
            _state.update {
                it.copy(
                    isLoadingDetails = false,
                    isLoadingCast = false,
                    error = detailsRes.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
            return@launch
        }

        //CAST
        val castRes = castDeferred.await()
        if (castRes.isSuccess) {
            _state.update {
                it.copy(characters = castRes.getOrNull().orEmpty(), isLoadingCast = false)
            }
        } else {
            _state.update {
                it.copy(isLoadingCast = false) // fail silently for cast
            }
        }
    }

    companion object {
        fun provide(
            owner: androidx.lifecycle.ViewModelStoreOwner,
            appContext: android.content.Context
        ): DetailViewModel {
            return ViewModelProvider(owner, object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DetailViewModel(AnimeRepository(appContext)) as T
                }
            })[DetailViewModel::class.java]
        }
    }
}