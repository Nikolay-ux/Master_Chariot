package com.nikolayux.masterchariot.feature.main

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.R
import com.nikolayux.masterchariot.feature.car.list.ui.CarListScreenRoute
import com.nikolayux.masterchariot.feature.functions.ui.FunctionScreenRoute
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
    modifier: Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.Diagnostics) }
    Scaffold(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
//        topBar = {
//            TopAppBar(
//                colors = TopAppBarColors(
//                    MaterialTheme.colorScheme.background,
//                    scrolledContainerColor = MaterialTheme.colorScheme.primary,
//                    navigationIconContentColor = MaterialTheme.colorScheme.secondary,
//                    titleContentColor = MaterialTheme.colorScheme.tertiary,
//                    actionIconContentColor = MaterialTheme.colorScheme.tertiary,
//                    subtitleContentColor = MaterialTheme.colorScheme.tertiary
//                ),
//                title = {
//                    Text(stringResource(R.string.app_name))
//                }
//            )
//        },
//        bottomBar = {
//            BottomAppBar(
//                containerColor = MaterialTheme.colorScheme.background
//            ) {
//                Tab.entries.forEach { tab ->
//                    NavigationBarItem(
//                        selected = selectedTab == tab,
//                        onClick = { selectedTab = tab },
//                        icon = {
//                            Icon(
//                                tab.icon,
//                                contentDescription = stringResource(tab.titleRes)
//                            )
//                        },
//                        label = { Text(stringResource(tab.titleRes)) }
//                    )
//                }
//            }
//        },
//        floatingActionButton = {
//            when (selectedTab) {
//                Tab.Diagnostics -> {
//                    FloatingActionButton(onClick = {
//                        navController.navigate(Navigation.Connect)
//                    }) {
//                        Text(stringResource(R.string.connect))
//                    }
//                }
//
//                Tab.Settings -> {}
//            }
//        }
    ) { insets ->
//        val postsListState = rememberLazyListState()
        Crossfade(modifier = modifier.fillMaxSize(), targetState = selectedTab) { tab ->
            when (tab) {
                Tab.Diagnostics -> FunctionScreenRoute(
                    modifier = modifier,
                    contentPadding = PaddingValues(
                        top = insets.calculateTopPadding(),
                        bottom = insets.calculateBottomPadding()
                    ),
                    navController = navController
                )

                Tab.Settings -> CarListScreenRoute(
                    modifier = modifier,
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
        MainScreen(modifier = Modifier)
    }
}