package com.example.aivideonote.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aivideonote.screen.auth.AuthViewModel

@Composable
fun CustomQuizScreen(authViewModel: AuthViewModel = hiltViewModel(), navController: NavController) {
    Column (Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Yes, it is working")
        Button(
            onClick = { authViewModel.addMCQs("Jai Shree Ram") }
        ) {
            Text("Click to Add")
        }
        Button(onClick = {
            navController.navigateUp()
        }) { }
    }
}