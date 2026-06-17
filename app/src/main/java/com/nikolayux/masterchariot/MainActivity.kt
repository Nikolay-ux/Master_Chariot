package com.nikolayux.masterchariot

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.nikolayux.masterchariot.ui.theme.MasterChariotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var navController: NavController
    private var sharedText: String? = null

    @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }

            MasterChariotTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                this@MainActivity.navController = navController

                NavigationScreen(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            intent.removeExtra(Intent.EXTRA_TEXT)
        }
    }
}
