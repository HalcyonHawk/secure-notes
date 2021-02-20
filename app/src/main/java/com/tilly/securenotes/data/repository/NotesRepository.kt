package com.tilly.securenotes.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tilly.securenotes.data.model.Note
import kotlin.collections.ArrayList

object NotesRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Notes collection name constant
    private const val NOTES_COLLECTION = "notes"

    // Get current user ID
    private fun getUserId(): String{
        return auth.uid!!
    }

    class FirebaseResponse<T>(val result: T){

    }

    // Load notes for current user and post arraylist of newly created note objects to livedata from viewmodel
    fun loadNotes(noteListLiveData: MutableLiveData<ArrayList<Note>>){
        firestore.collection(NOTES_COLLECTION)
            .whereEqualTo("user_id", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {result ->
                val noteList = arrayListOf<Note>()
                for(document in result){
                    noteList.add(Note(noteId = document.id,
                        title = document.getString("title")!!,
                        content = document.getString("content")!!,
                        lastEdited = document.getDate("last_edited_date")!!))
                }
                noteListLiveData.postValue(noteList)
            }
            .addOnFailureListener{
                Log.e("firebase", "Error getting documents", it)
            }
    }

    // Submit note to firebase
    fun createNoteOnFirebase(newNote: Note){
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited)

        firestore.collection(NOTES_COLLECTION)
            .add(noteHashMap)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
    }

    // Edit existing note on firebase by note document ID
    fun editNoteOnFirebase(newNote: Note){
        val noteHashMap = hashMapOf(
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited)

        firestore.collection(NOTES_COLLECTION)
            .document(newNote.noteId)
            .set(noteHashMap)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
    }

    fun deleteNote(id: String): Task<Void> {
        return firestore.collection(NOTES_COLLECTION).document(id).delete()
    }
}