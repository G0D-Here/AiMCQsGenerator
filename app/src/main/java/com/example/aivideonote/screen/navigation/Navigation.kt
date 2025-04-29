package com.example.aivideonote.screen.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aivideonote.screen.CustomQuizScreen
import com.example.aivideonote.screen.GeminiViewModel
import com.example.aivideonote.screen.MainScreen
import com.example.aivideonote.screen.auth.AuthViewModel
import kotlinx.serialization.Serializable

@Serializable
data class CustomQuizScreen(val uid: String = "")

@Serializable
object MainScreen

@Serializable
object ProfileScreen

@Composable
fun NavGraph(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = MainScreen) {
        composable<MainScreen> {
            val viewModel: GeminiViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            MainScreen(
                modifier = modifier,
                viewModel = viewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }
        composable<CustomQuizScreen>(
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it })
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it })
            }
        ) {
            val authViewModel: AuthViewModel = hiltViewModel()
            CustomQuizScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}