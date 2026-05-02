package com.nikolayux.masterchariot

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
//import androidx.navigation.toRoute
import com.nikolayux.masterchariot.feature.main.MainScreen
import kotlinx.serialization.Serializable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationScreen(
    navController: NavHostController,
//    sharedText: String? = null
) {

//    LaunchedEffect(sharedText) {
//        if (!sharedText.isNullOrEmpty()) {
//            navController.navigate(Navigation.NewEvent(id = -1L, text = sharedText))
//        }
//    }

    NavHost(navController = navController, startDestination = Navigation.Main) {
        composable<Navigation.Main> {
            MainScreen()
        }
    }
}

@Serializable
sealed interface Navigation {
    @Serializable
    object Main : Navigation
}