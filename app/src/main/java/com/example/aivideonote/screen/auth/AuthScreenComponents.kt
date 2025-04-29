package com.example.aivideonote.screen.auth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aivideonote.data.remote.AuthUiState
import com.example.aivideonote.data.remote.LoginRequest
import com.example.aivideonote.data.remote.RegisterRequest
import com.example.aivideonote.data.remote.UsernameValidationState
import com.example.aivideonote.data.remote.toUserMessage
import java.util.Locale

const val GET_KEY = "https://makersuite.google.com/app/apikey"


@Composable
fun AuthScreenComponents(viewModel: AuthViewModel = hiltViewModel()) {
    val usernameState by viewModel.usernameAvailabilityState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val interaction = remember { MutableInteractionSource() }

    var key by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    val context = LocalContext.current



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "MCQs\nGenerator",
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.displaySmall,
                fontFamily = FontFamily.Serif,
                lineHeight = 36.sp
            )

            // Auth Card
            ElevatedCard(
                elevation = CardDefaults.elevatedCardElevation(2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Email Field
                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "eg. pranav@gmail.com"
                    )

                    // Password Field
                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Enter password",
                        isPassword = true
                    )
                    AuthTextField(
                        value = key,
                        onValueChange = {
                            key = it

                        },
                        label = "Key",
                        placeholder = "Enter Your Gemini Key",
                        trailingIcon = {
                            Text("Get Key", Modifier.clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GET_KEY))
                                context.startActivity(intent)
                            })
                        }
                    )
                    // Registration Fields (only shown in sign-up mode)
                    AnimatedVisibility(visible = !isLoginMode) {
                        Column(verticalArrangement = Arrangement.Center) {
                            // Name Field
                            AuthTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = "Name",
                                placeholder = "Enter name"
                            )

                            // Username Field with validation
                            AuthTextField(
                                value = username,
                                onValueChange = {
                                    username = it.lowercase(Locale.ROOT)
                                    if (it.length > 3) viewModel.checkUsernameAvailability(it.trim())
                                },
                                label = "Username",
                                placeholder = "Enter username",
                                trailingIcon = {
                                    when (usernameState) {
                                        UsernameValidationState.Loading ->
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))

                                        is UsernameValidationState.Available ->
                                            Icon(
                                                imageVector = if ((usernameState as UsernameValidationState.Available).isAvailable)
                                                    Icons.Default.Check
                                                else
                                                    Icons.Default.Close,
                                                contentDescription = "Availability",
                                                tint = if ((usernameState as UsernameValidationState.Available).isAvailable)
                                                    MyColors().green
                                                else
                                                    Color.Red
                                            )

                                        else -> {}
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Auth Button
            ElevatedCard(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick =
                        {
                            if (isLoginMode && email.isNotEmpty() && password.isNotEmpty()) {
                                viewModel.login(LoginRequest(email.trim(), password.trim()))
                            } else {
                                if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && key.isNotEmpty())
                                    viewModel.registerUser(
                                        RegisterRequest(
                                            name.trim(),
                                            username.trim(),
                                            email.trim(),
                                            password.trim(),
                                            key = key.trim()
                                        )
                                    )
                            }
                        }
                    ),
                colors = CardDefaults.elevatedCardColors(
                    MyColors().green
                )
            ) {
                Box(Modifier.size(80.dp, 35.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (isLoginMode) "Sign In" else "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W600,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                }
            }


            Text(
                text = if (isLoginMode) "Create an account" else "Already have an account?",
                modifier = Modifier
                    .padding(10.dp)
                    .clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = {
                            isLoginMode = !isLoginMode
                        }),
            )


            // Error Message
            if (authState is AuthUiState.Error) {
                Text(
                    text = (authState as AuthUiState.Error).error.toUserMessage(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Loading Indicator
        if (authState is AuthUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

private fun validateFields(
    isLoginMode: Boolean,
    email: String,
    password: String,
    name: String,
    username: String,
    usernameState: UsernameValidationState
): Boolean {
    return when {
        email.isBlank() || password.isBlank() -> false
        !isLoginMode && name.isBlank() -> false
        !isLoginMode && (username.isBlank() ||
                usernameState !is UsernameValidationState.Available ||
                !(usernameState as UsernameValidationState.Available).isAvailable) -> false

        else -> true
    }

}

class MyColors(
    val green: Color = Color(0xFF08850A),
    val maroon: Color = Color(0xFF85081B)

)


