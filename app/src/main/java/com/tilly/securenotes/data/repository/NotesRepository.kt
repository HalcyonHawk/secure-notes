package com.tilly.securenotes.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tilly.securenotes.data.model.Note
import java.util.*
import kotlin.collections.ArrayList

object NotesRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun getUserId(): String{
        return auth.uid!!
    }

    fun loadNotes(noteListLiveData: MutableLiveData<ArrayList<Note>>){
        firestore.collection("notes")
            .whereEqualTo("user_id", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {result ->
                val noteList = arrayListOf<Note>()
                for(document in result){
                    noteList.add(Note(noteId = document.id,
                        userId = document.getString("user_id")!!,
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

    fun postNote(title: String,
                 content: String){

        val note = hashMapOf("user_id" to getUserId(),
            "title" to title, "content" to content,
            "last_edited_date" to Calendar.getInstance().time)
        firestore.collection("notes")
            .add(note)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
    }
}