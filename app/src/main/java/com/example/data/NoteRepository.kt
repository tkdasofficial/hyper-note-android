package com.example.data

import com.example.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val noteDao: NoteDao, private val cryptoManager: CryptoManager) {
    
    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { notes ->
            notes.map { note ->
                if (note.isEncrypted) {
                    note.copy(
                        title = cryptoManager.decryptString(note.title),
                        content = cryptoManager.decryptString(note.content)
                    )
                } else {
                    note
                }
            }
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        val note = noteDao.getNoteById(id) ?: return null
        return if (note.isEncrypted) {
            note.copy(
                title = cryptoManager.decryptString(note.title),
                content = cryptoManager.decryptString(note.content)
            )
        } else {
            note
        }
    }

    suspend fun insertNote(note: Note) {
        val encryptedNote = note.copy(
            title = cryptoManager.encryptString(note.title),
            content = cryptoManager.encryptString(note.content),
            isEncrypted = true
        )
        noteDao.insertNote(encryptedNote)
    }

    suspend fun updateNote(note: Note) {
        val encryptedNote = note.copy(
            title = cryptoManager.encryptString(note.title),
            content = cryptoManager.encryptString(note.content),
            isEncrypted = true
        )
        noteDao.updateNote(encryptedNote)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }
}
