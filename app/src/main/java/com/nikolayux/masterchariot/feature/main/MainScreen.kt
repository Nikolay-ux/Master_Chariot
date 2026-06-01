package com.nikolayux.masterchariot.feature.main

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.Navigation
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.ui.CarListScreenRoute
import com.nikolayux.masterchariot.feature.functions.ui.FunctionScreen
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

enum class Tab(
    @param:StringRes val titleRes: Int,
    val icon: ImageVector
) {
    Diagnostics(R.string.tab_diagnostics, Icons.Default.Build),
    Settings(R.string.tab_settings, Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Diagnostics) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, null)
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                tab.icon,
                                contentDescription = stringResource(tab.titleRes)
                            )
                        },
                        label = { Text(stringResource(tab.titleRes)) }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                Tab.Diagnostics -> {
                    FloatingActionButton(onClick = {
//                        navController.navigate(Navigation.Connect)
                    }) {
                        Text(stringResource(R.string.connect))
                    }
                }

                Tab.Settings -> {}
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { insets ->
//        val postsListState = rememberLazyListState()
        Crossfade(modifier = Modifier.fillMaxSize(), targetState = selectedTab) { tab ->
            when (tab) {
                Tab.Diagnostics -> FunctionScreen(
                    contentPadding = PaddingValues(
                        top = insets.calculateTopPadding(),
                        bottom = insets.calculateBottomPadding()
                    ),
//                    navController = navController
                )

                Tab.Settings -> CarListScreenRoute(
                    contentPadding = PaddingValues(
                        top = insets.calculateTopPadding(),
                        bottom = insets.calculateBottomPadding()
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MasterChariotTheme {
        MainScreen()
    }
}