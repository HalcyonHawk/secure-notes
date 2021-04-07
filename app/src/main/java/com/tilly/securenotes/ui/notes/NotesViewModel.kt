package com.tilly.securenotes.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.tilly.securenotes.data.Note
import com.tilly.securenotes.data.repository.NoteRepository

// ViewModel for notes activity, holds a list of notes using live data and contains function to update
// the notes from firebase.
class NotesViewModel: ViewModel() {

    // Notes list live data to post updates to observers
    private val _notesList: MutableLiveData<ArrayList<Note>> = MutableLiveData(arrayListOf())
    // Getter to only expose unmutable live data object to view
    val notesList: LiveData<ArrayList<Note>> get() = _notesList

    // Load new notes into live data ArrayList  from firebase using repository if successful
    // post new notes list to live data to update view
    fun loadNotes(): Task<QuerySnapshot> {
        // Loading notes
        return NoteRepository.loadNotes()
            .addOnSuccessListener {result ->
                // Converting notes documents from firestore database to list of Note objects
                val noteList = arrayListOf<Note>()
                for(document in result){
                    noteList.add(
                        Note(noteId = document.id,
                        title = document.getString("title")!!,
                        content = document.getString("content")!!,
                        lastEdited = document.getDate("last_edited_date")!!,
                        favourite = document.getBoolean("is_favourite")!!)
                    )
                }
                // Post successful response with note list
                _notesList.postValue(noteList)
            }
    }
}