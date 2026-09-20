package com.example.cpen321application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cpen321application.ui.ConnectionScreen
import com.example.cpen321application.ui.MainScreen
import com.example.cpen321application.ui.PictureScreen
import com.example.cpen321application.ui.SurpriseScreen
import com.example.cpen321application.ui.theme.CPEN321ApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CPEN321ApplicationTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main_screen") {
                            MainScreen(navController = navController)
                        }
                        composable("connection_screen") {
                            ConnectionScreen()
                        }
                        composable("picture_screen") {
                            PictureScreen()
                        }
                        composable("surprise_screen") {
                            SurpriseScreen()
                        }
                    }
                }
            }
        }
    }
}
