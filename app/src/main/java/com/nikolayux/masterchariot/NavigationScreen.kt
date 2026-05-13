package com.nikolayux.masterchariot

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikolayux.masterchariot.feature.connect.ui.ConnectScreenRoute
//import androidx.navigation.toRoute
import com.nikolayux.masterchariot.feature.main.MainScreen
import kotlinx.serialization.Serializable

@androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT])
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
            MainScreen(navController)
        }

        composable<Navigation.Connect> {
            ConnectScreenRoute(modifier = Modifier, navController = navController)
        }
    }
}

@Serializable
sealed interface Navigation {
    @Serializable
    object Main : Navigation

    @Serializable
    object Connect : Navigation
}