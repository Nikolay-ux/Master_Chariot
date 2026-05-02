package com.nikolayux.masterchariot.feature.functions.ui

import androidx.compose.runtime.Immutable

@Immutable
data class FunctionListState (
    val functions: List<FunctionUiModel> = emptyList(),
    val groupedFunctions: Map<Long, List<FunctionUiModel>> = emptyMap()
)