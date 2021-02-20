package com.tilly.securenotes.ui.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.LoginRepository
import com.tilly.securenotes.data.repository.NotesRepository
import java.util.*
import kotlin.collections.ArrayList

class NotesViewModel: ViewModel() {
    val notesList: MutableLiveData<ArrayList<Note>> = MutableLiveData(arrayListOf())


    // Load notes into live data arraylist from firebase using repository
    fun loadNotes(){
        NotesRepository.loadNotes(notesList)
    }

    fun addDummyNote(num: Int){
        NotesRepository.postNote(title = "Note number $num",
            content = "Note body")
        loadNotes()
    }

}