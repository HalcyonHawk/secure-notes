package com.tilly.securenotes.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.utilities.NotesUtility
import kotlin.collections.ArrayList

object NoteRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Notes collection name constant
    private const val NOTES_COLLECTION = "notes"


    // Get current user ID
    private fun getUserId(): String{
        return auth.uid!!
    }


    // Load notes for current user and pass async task to viewmodel
    //TODO: Change to new ResultStatusWrapper
    fun loadNotes(): LiveData<ResultStatusWrapper<ArrayList<Note>>> {
        val liveDataWrapper: MutableLiveData<ResultStatusWrapper<ArrayList<Note>>> = MutableLiveData()
        if (auth.currentUser != null){
            firestore.collection(NOTES_COLLECTION)
                .whereEqualTo("user_id", auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener {result ->
                    val noteList = arrayListOf<Note>()
                    for(document in result){
                        noteList.add(Note(noteId = document.id,
                            title = document.getString("title")!!,
                            content = document.getString("content")!!,
                            lastEdited = document.getDate("last_edited_date")!!,
                            favorite = document.getBoolean("is_favorite")!!))
                    }
                    // Post successful response with note list
                    liveDataWrapper.postValue(ResultStatusWrapper.Success(noteList))
                }
                .addOnFailureListener{ exception ->
                    liveDataWrapper.postValue(ResultStatusWrapper.Error(null, exception))
                }
        }
        return liveDataWrapper
    }

    // Submit note to firebase and update current note id returns id for new note
    fun createNoteOnFirebase(newNote: Note): Task<DocumentReference> {
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited,
            "is_favorite" to false)
        return firestore.collection(NOTES_COLLECTION)
            .add(noteHashMap)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
    }

    // Edit existing note on firebase by note document ID
    fun editNoteOnFirebase(newNote: Note): Task<Void> {
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited,
            "is_favorite" to newNote.favorite)

        return firestore.collection(NOTES_COLLECTION)
            .document(newNote.noteId)
            .set(noteHashMap)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
    }



    fun favoriteNote(noteId: String, favorite: Boolean): Task<Void>{
        return firestore.collection(NOTES_COLLECTION)
            .document(noteId)
            .update("is_favorite", favorite)
    }

    fun deleteNote(id: String): Task<Void> {
        return firestore.collection(NOTES_COLLECTION).document(id).delete()
    }
}