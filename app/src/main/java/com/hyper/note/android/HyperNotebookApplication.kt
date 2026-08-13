package com.hyper.note.android

import android.app.Application
import com.hyper.note.android.data.NoteDatabase
import com.hyper.note.android.data.NoteRepository
import com.hyper.note.android.security.CryptoManager
import com.google.firebase.FirebaseApp

class HyperNotebookApplication : Application() {
    val database by lazy { NoteDatabase.getDatabase(this) }
    val cryptoManager by lazy { CryptoManager() }
    val repository by lazy { NoteRepository(database.noteDao(), cryptoManager) }
    val userPreferences by lazy { com.hyper.note.android.data.UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
