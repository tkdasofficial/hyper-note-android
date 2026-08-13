package com.example

import android.app.Application
import com.example.data.NoteDatabase
import com.example.data.NoteRepository
import com.example.security.CryptoManager
import com.google.firebase.FirebaseApp

class EliteNotebookApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
    val cryptoManager by lazy { CryptoManager() }
    val repository by lazy { NoteRepository(database.noteDao(), cryptoManager) }
    val userPreferences by lazy { com.example.data.UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
