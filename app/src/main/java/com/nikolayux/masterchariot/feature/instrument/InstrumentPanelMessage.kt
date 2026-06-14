package com.nikolayux.masterchariot.feature.instrument

sealed interface InstrumentPanelMessage {
    data object BackClicked : InstrumentPanelMessage
}
