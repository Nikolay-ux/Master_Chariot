package com.nikolayux.masterchariot.feature.connect.state

import android.content.Intent
import androidx.annotation.StringRes

sealed interface ConnectEffect {
    data object Connected : ConnectEffect
    data class ShowToast(@StringRes val messageResId: Int) : ConnectEffect
    data class RequestBluetoothEnable(val intent: Intent) : ConnectEffect
//    data object PermissionRequired : ConnectEffect
}