package com.example.dydemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dydemo.ui.screens.ChatScreen
import com.example.dydemo.ui.screens.MessageListScreen
import com.example.dydemo.ui.screens.SearchScreen
import com.example.dydemo.ui.theme.DyDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DyDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "message_list") {
        composable("message_list") {
            MessageListScreen(
                onNavigateToChat = { userId ->
                    navController.navigate("chat/$userId")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                }
            )
        }
        composable(
            route = "chat/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("search") {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { userId ->
                    navController.navigate("chat/$userId")
                }
            )
        }
    }
}
