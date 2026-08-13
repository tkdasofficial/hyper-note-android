package com.hyper.note.android

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hyper.note.android.ui.HomeScreen
import com.hyper.note.android.ui.NoteDetailScreen
import com.hyper.note.android.ui.NoteViewModel
import com.hyper.note.android.ui.NoteViewModelFactory
import com.hyper.note.android.ui.theme.MyApplicationTheme

import com.hyper.note.android.auth.AuthManager

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as HyperNotebookApplication
        val viewModelFactory = NoteViewModelFactory(app.repository)
        val userPreferences = app.userPreferences
        
        setContent {
            val authManager = androidx.compose.runtime.remember { AuthManager(this) }
            val themeMode by userPreferences.theme.collectAsState()
            
            MyApplicationTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: NoteViewModel = viewModel(factory = viewModelFactory)
                    val notes by viewModel.notes.collectAsState()
                    
                    val appSecurityEnabled by userPreferences.appSecurityEnabled.collectAsState()
                    val encryptionKey by userPreferences.encryptionKey.collectAsState()
                    val enableBiometrics by userPreferences.enableBiometrics.collectAsState()
                    var isUnlocked by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

                    if (appSecurityEnabled && !isUnlocked) {
                        com.hyper.note.android.ui.AppLockScreen(
                            correctPin = encryptionKey,
                            enableBiometrics = enableBiometrics,
                            isAuthenticated = authManager.currentUser != null,
                            onUnlocked = { isUnlocked = true },
                            onPinReset = { newPin -> 
                                userPreferences.saveEncryptionKey(newPin)
                                isUnlocked = true
                            }
                        )
                    } else {
                        val navController = rememberNavController()
                        NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                notes = notes,
                                userPreferences = userPreferences,
                                authManager = authManager,
                                onAddNote = {
                                    navController.navigate("note_detail/-1")
                                },
                                onNoteClick = { note ->
                                    navController.navigate("note_detail/${note.id}")
                                },
                                onEraseAll = {
                                    viewModel.deleteAllNotes()
                                }
                            )
                        }
                        composable("note_detail/{noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: -1
                            val note = notes.find { it.id == noteId }
                            NoteDetailScreen(
                                note = note,
                                onSave = { title, content, isVoice ->
                                    if (note != null) {
                                        viewModel.updateNote(note.copy(title = title, content = content, isVoiceNote = isVoice))
                                    } else {
                                        if (title.isNotEmpty() || content.isNotEmpty()) {
                                            viewModel.addNote(title, content, isVoice)
                                        }
                                    }
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
}

