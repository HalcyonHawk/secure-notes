package com.tilly.securenotes.data.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tilly.securenotes.data.Note

// Note Repository object - Provides access to note related data sources
object NoteRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Notes collection name constant
    private const val NOTES_COLLECTION = "notes"

    // Get current user ID
    private fun getUserId(): String{
        return auth.uid!!
    }

    // Load notes for current user and return task to ViewModel for handling
    fun loadNotes(): Task<QuerySnapshot> {
        return firestore.collection(NOTES_COLLECTION)
                .whereEqualTo("user_id", auth.currentUser!!.uid)
                .get()
    }

    // Submit note to firebase. Returns note_id for newly created note
    fun createNoteOnFirebase(newNote: Note): Task<DocumentReference> {
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited,
            "is_favourite" to false)
        return firestore.collection(NOTES_COLLECTION)
            .add(noteHashMap)
            .addOnFailureListener{
                // Logging error if note creation failed
                Log.e("firebase", "Error creating document", it)
            }
    }

    // Edit existing note on firebase by note document ID
    fun editNoteOnFirebase(newNote: Note): Task<Void> {
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited,
            "is_favourite" to newNote.favourite)

        return firestore.collection(NOTES_COLLECTION)
            .document(newNote.noteId)
            .set(noteHashMap)
            .addOnFailureListener{
                // Logging error if note editing failed
                Log.e("firebase", "Error editing document", it)
            }
    }

    // Update is_favourite field for note in database
    fun favouriteNote(noteId: String, favourite: Boolean): Task<Void>{
        return firestore.collection(NOTES_COLLECTION)
            .document(noteId)
            .update("is_favourite", favourite)
    }

    // Delete note from database
    fun deleteNote(id: String): Task<Void> {
        return firestore.collection(NOTES_COLLECTION).document(id).delete()
    }
}