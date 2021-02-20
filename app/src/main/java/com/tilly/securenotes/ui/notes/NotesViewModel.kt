package com.tilly.securenotes.ui.notes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NotesRepository
import kotlin.collections.ArrayList

class NotesViewModel: ViewModel() {
    val notesList: MutableLiveData<ArrayList<Note>> = MutableLiveData(arrayListOf())


    // Load notes into live data arraylist from firebase using repository
    fun loadNotes(){
        NotesRepository.loadNotes(notesList)
    }


}