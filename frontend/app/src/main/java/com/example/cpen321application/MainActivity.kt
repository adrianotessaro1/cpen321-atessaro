package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cpen321application.ui.ConnectionScreen
import com.example.cpen321application.ui.MainScreen
import com.example.cpen321application.ui.PictureScreen
import com.example.cpen321application.ui.SurpriseScreen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

private const val MAIN_ROUTE = "main_screen"

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Main theme of the app
            CPEN321ApplicationTheme {
                // Instance of the navigation controller to move between screens
                val navController = rememberNavController()

                // Which screen is showing right now.
                val currentEntry by navController.currentBackStackEntryAsState()
                val showBack = currentEntry?.destination?.route != MAIN_ROUTE

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showBack) {
                            TopAppBar(
                                title = {},
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    // NavHost is the main container for the navigation
                    NavHost(
                        navController = navController,
                        startDestination = MAIN_ROUTE,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Main screen
                        composable(MAIN_ROUTE) {
                            MainScreen(navController = navController)
                        }
                        // Connection screen
                        composable("connection_screen") {
                            ConnectionScreen()
                        }
                        // Picture screen
                        composable("picture_screen") {
                            PictureScreen()
                        }
                        // Surprise screen
                        composable("surprise_screen") {
                            SurpriseScreen()
                        }
                    }
                }
            }
        }
    }
}
