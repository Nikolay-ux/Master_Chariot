package com.nikolayux.masterchariot.feature.functions.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FunctionUiModel(
    val id: Long = 0,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
) : Parcelable
