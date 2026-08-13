package com.hyper.note.android.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun AppLockScreen(
    correctPin: String,
    enableBiometrics: Boolean,
    isAuthenticated: Boolean,
    onUnlocked: () -> Unit,
    onPinReset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isResettingPin by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = generateSequence(context) {
        if (it is android.content.ContextWrapper) it.baseContext else null
    }.firstOrNull { it is FragmentActivity } as? FragmentActivity

    if (isResettingPin) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Reset PIN", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = newPin,
                onValueChange = { newPin = it },
                label = { Text("New PIN (4-16 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmNewPin,
                onValueChange = { confirmNewPin = it },
                label = { Text("Confirm New PIN") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (newPin.length in 4..16) {
                        if (newPin == confirmNewPin) {
                            onPinReset(newPin)
                        } else {
                            errorMessage = "PINs do not match"
                        }
                    } else {
                        errorMessage = "PIN must be 4-16 digits"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set New PIN")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { isResettingPin = false; errorMessage = "" }) {
                Text("Cancel")
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Lock",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "App Locked",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = enteredPin,
            onValueChange = { 
                enteredPin = it
                if (it == correctPin) {
                    onUnlocked()
                } else if (it.length >= correctPin.length) {
                    errorMessage = "Incorrect PIN"
                } else {
                    errorMessage = ""
                }
            },
            label = { Text("Enter PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            isError = errorMessage.isNotEmpty(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                if (!isAuthenticated) {
                    errorMessage = "Must be logged in with Google to reset PIN."
                    return@TextButton
                }
                if (activity != null) {
                    val biometricManager = BiometricManager.from(activity)
                    val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                        val executor = ContextCompat.getMainExecutor(activity)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    isResettingPin = true
                                    errorMessage = ""
                                }
                            })
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Verify Identity")
                            .setSubtitle("Confirm your device credentials to reset PIN")
                            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            .build()
                        biometricPrompt.authenticate(promptInfo)
                    } else {
                        errorMessage = "Device credentials not setup. Cannot reset PIN."
                    }
                }
            }
        ) {
            Text("Forgot PIN?")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (enableBiometrics && activity != null) {
            val biometricManager = BiometricManager.from(activity)
            val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                IconButton(
                    onClick = {
                        val executor = ContextCompat.getMainExecutor(activity)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    onUnlocked()
                                }
                            }
                        )

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Unlock Hyper Notebook")
                            .setSubtitle("Authenticate using your biometric credential")
                            .setNegativeButtonText("Use PIN")
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Use Biometrics",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to use fingerprint",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}
