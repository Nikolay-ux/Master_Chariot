package com.nikolayux.masterchariot.feature.functions.ui

sealed interface FunctionListMessage {
    data object ConnectAndStart : FunctionListMessage
    data object Disconnect : FunctionListMessage
}