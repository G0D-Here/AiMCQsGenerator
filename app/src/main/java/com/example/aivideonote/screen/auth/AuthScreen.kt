package com.example.aivideonote.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aivideonote.data.remote.AuthUiState
import com.example.aivideonote.data.remote.toUserMessage
import com.example.aivideonote.screen.navigation.NavGraph

@Composable
fun Auth(viewModel: AuthViewModel = hiltViewModel(), modifier: Modifier) {
    val authState by viewModel.authState.collectAsState()
    authState.let {
        when (it) {

            is AuthUiState.Error -> {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(it.error.toUserMessage())
                    Button(onClick = { viewModel.logout() }) {
                        Text("Logout guys")
                    }
                }
            }

            AuthUiState.Idle -> AuthScreenComponents(viewModel)
            AuthUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AuthUiState.Success -> {
                Scaffold { innerPadding ->
                    NavGraph(Modifier.padding(innerPadding))
                    apikeyToUse = it.data.key
                }
            }
        }
    }

}