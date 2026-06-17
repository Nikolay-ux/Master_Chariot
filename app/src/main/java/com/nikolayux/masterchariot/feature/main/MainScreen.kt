package com.nikolayux.masterchariot.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.feature.functions.ui.FunctionScreenRoute
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme

@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
    modifier: Modifier
) {
    FunctionScreenRoute(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        navController = navController
    )
}

@Preview
@Composable
fun MainScreenPreview() {
    MasterChariotTheme {
        MainScreen(modifier = Modifier)
    }
}