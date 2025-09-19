package com.seekho.animeapp.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Settings {
    private val _hideImages = MutableStateFlow(false)

    val hideImages: StateFlow<Boolean> = _hideImages

    fun setHideImages(value: Boolean) {
        _hideImages.value = value
    }
}