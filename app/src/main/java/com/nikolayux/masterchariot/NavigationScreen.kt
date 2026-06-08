package com.nikolayux.masterchariot

//import androidx.compose.runtime.LaunchedEffect
//import androidx.navigation.toRoute
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikolayux.masterchariot.feature.car.list.ui.CarListScreenRoute
import com.nikolayux.masterchariot.feature.connect.ui.ConnectScreenRoute
import com.nikolayux.masterchariot.feature.main.MainScreen
import com.nikolayux.masterchariot.feature.trip.TripScreenRoute
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
            MainScreen(modifier = Modifier, navController = navController)
        }

        composable<Navigation.Connect> {
            ConnectScreenRoute(modifier = Modifier, navController = navController)
        }

        composable<Navigation.InstrumentPanel> {
            TripScreenRoute(modifier = Modifier, navController = navController)
        }

        composable<Navigation.Settings> {
            CarListScreenRoute(
                modifier = Modifier, navController = navController
            )
        }
    }
}

@Serializable
sealed interface Navigation {
    @Serializable
    object Main : Navigation

    @Serializable
    object Connect : Navigation

    @Serializable
    object InstrumentPanel

    @Serializable
    object Settings
}