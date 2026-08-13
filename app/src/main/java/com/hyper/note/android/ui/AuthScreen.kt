package com.hyper.note.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hyper.note.android.auth.AuthManager
import com.hyper.note.android.auth.AuthResult
import kotlinx.coroutines.launch

enum class AuthStep {
    EMAIL, LOGIN, SIGN_UP
}

@Composable
fun AuthScreen(
    authManager: AuthManager,
    onAuthSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var authStep by remember { mutableStateOf(AuthStep.EMAIL) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (authStep) {
                AuthStep.EMAIL -> "Get Started"
                AuthStep.LOGIN -> "Welcome Back"
                AuthStep.SIGN_UP -> "Create Account"
            },
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = authStep == AuthStep.EMAIL || isLoading.not()
        )
        
        if (authStep != AuthStep.EMAIL) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                    }
                }
            )
        }
        
        if (authStep == AuthStep.SIGN_UP) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; errorMessage = null },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }
        
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (email.isBlank()) {
                    errorMessage = "Please enter your email"
                    return@Button
                }
                
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    
                    when (authStep) {
                        AuthStep.EMAIL -> {
                            val result = authManager.checkEmailExists(email)
                            if (result) {
                                authStep = AuthStep.LOGIN
                            } else {
                                authStep = AuthStep.SIGN_UP
                            }
                        }
                        AuthStep.LOGIN -> {
                            if (password.isBlank()) {
                                errorMessage = "Please enter your password"
                                isLoading = false
                                return@launch
                            }
                            val result = authManager.signInWithEmail(email, password)
                            if (result is AuthResult.Success) {
                                onAuthSuccess()
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message
                            }
                        }
                        AuthStep.SIGN_UP -> {
                            if (password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Please enter and confirm your password"
                                isLoading = false
                                return@launch
                            }
                            if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                isLoading = false
                                return@launch
                            }
                            val result = authManager.signUpWithEmail(email, password)
                            if (result is AuthResult.Success) {
                                onAuthSuccess()
                            } else if (result is AuthResult.Error) {
                                errorMessage = result.message
                            }
                        }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Please wait..." else "Continue")
        }
        
        if (authStep == AuthStep.LOGIN) {
            TextButton(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val res = authManager.resetPassword(email)
                        isLoading = false
                        if (res is AuthResult.Success) {
                            android.widget.Toast.makeText(context, "Password reset email sent", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (res is AuthResult.Error) {
                            errorMessage = res.message
                        }
                    }
                }
            ) {
                Text("Forgot Password?")
            }
        }
        
        if (authStep == AuthStep.EMAIL) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("OR", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        val success = authManager.signInWithGoogle()
                        isLoading = false
                        if (success) {
                            onAuthSuccess()
                        } else {
                            errorMessage = "Google Sign-In failed or was cancelled."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Continue with Google")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = {
            if (authStep != AuthStep.EMAIL) {
                authStep = AuthStep.EMAIL
                errorMessage = null
            } else {
                onBack()
            }
        }) {
            Text(if (authStep == AuthStep.EMAIL) "Cancel" else "Back")
        }
    }
}
