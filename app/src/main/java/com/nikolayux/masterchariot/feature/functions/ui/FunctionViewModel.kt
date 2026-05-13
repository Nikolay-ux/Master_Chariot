package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FunctionViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var state by mutableStateOf(
        FunctionListState(
            List(10, ::createEvent)
        )
    )
        private set
    private val _effects = MutableSharedFlow<FunctionListEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    fun action(message: FunctionListMessage) {
        when (message) {
            is FunctionListMessage.LikeClicked -> like(message.id)
        }
    }

    fun updateData() {
        val grouped = state.functions.sortedBy { it.id }.groupBy { it.id }
        state = state.copy(
            functions = state.functions, groupedFunctions = grouped
        )
    }

    private fun like(id: Long) {
        state = state.copy(
            functions = state.functions.map { currentEvent ->
                if (currentEvent.id == id) {
                    currentEvent.copy(
                        likes = if (currentEvent.likedByMe) {
                            currentEvent.likes - 1
                        } else {
                            currentEvent.likes + 1
                        }, likedByMe = !currentEvent.likedByMe
                    )
                } else {
                    currentEvent
                }
            })
        savedStateHandle[EVENT_KEY] = state.functions
    }

    private fun createEvent(id: Int) = FunctionUiModel(
        id = id.toLong(),
        likes = 2,
        likedByMe = true,
    )

    private companion object {
        const val EVENT_KEY = "event"
    }
}