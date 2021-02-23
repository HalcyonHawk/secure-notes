package com.tilly.securenotes.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.model.User
import java.io.FileNotFoundException
import kotlin.collections.ArrayList

object NoteRepository {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Notes collection name constant
    private const val NOTES_COLLECTION = "notes"
    private const val USERS_COLLECTION = "users"

    // Get current user ID
    private fun getUserId(): String{
        return auth.uid!!
    }

    fun getLoggedInUser(): User {
        val fbUser = AuthRepository.getFirebaseUser()!!
        return User(email = fbUser.email!!,
            name = fbUser.displayName!!,
            avatar = fbUser.photoUrl.toString())
    }


    fun getCurrentUser(): LiveData<ResultStatusWrapper<User>>{
        val userLiveData: MutableLiveData<ResultStatusWrapper<User>> = MutableLiveData()


        firestore.collection(USERS_COLLECTION)
            .whereEqualTo("external_id", getUserId())
            .get()
            .addOnSuccessListener { queryResult ->
                val documents = queryResult.documents
                // Check if user document with ID from current firebase auth user is found
                if (documents.isNotEmpty()){
                    val doc = documents.first()
                    val user = User(email = doc.getString("email")!!,
                        name = doc.getString("external_id")!!,
                        avatar = doc.getString("avatar")!!)
                    userLiveData.postValue(ResultStatusWrapper.Success(user))
                } else {
                    // If user not found, return error in result wrapper
                    userLiveData.postValue(ResultStatusWrapper.Error(null,
                        exception = NoSuchElementException("User not found")))
                }
            }
            .addOnFailureListener { exception ->
                userLiveData.postValue(ResultStatusWrapper.Error(null, exception))
            }

        return userLiveData
    }

    // Load notes for current user and pass async task to viewmodel
    //TODO: Change to new ResultStatusWrapper
    fun loadNotes(): LiveData<ResultStatusWrapper<ArrayList<Note>>> {
        val liveDataWrapper: MutableLiveData<ResultStatusWrapper<ArrayList<Note>>> = MutableLiveData()
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
                // Post successful response with note list
                liveDataWrapper.postValue(ResultStatusWrapper.Success(noteList))
            }
            .addOnFailureListener{ exception ->
                liveDataWrapper.postValue(ResultStatusWrapper.Error(null, exception))
            }
        return liveDataWrapper
    }

    // Submit note to firebase and update current note id returns id for new note
    fun createNoteOnFirebase(newNote: Note): String{
        val noteHashMap = hashMapOf("user_id" to getUserId(),
            "title" to newNote.title,
            "content" to newNote.content,
            "last_edited_date" to newNote.lastEdited)
        var newId: String = ""
        firestore.collection(NOTES_COLLECTION)
            .add(noteHashMap)
            .addOnFailureListener{
                Log.e("firebase", "Error creating document", it)
            }
        return newId
    }

    // Edit existing note on firebase by note document ID
    fun editNoteOnFirebase(newNote: Note){
        val noteHashMap = hashMapOf("user_id" to getUserId(),
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