package com.tilly.securenotes.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NotesRepository
import com.tilly.securenotes.ui.notes.NotesUtility
import java.util.*

class EditorViewModel: ViewModel() {
    // Is the note being edited
    private var isEditing = false

    // Declare timezone and locale for formatting date and time
    private lateinit var locale: Locale
    private lateinit var timeZone: TimeZone

    // Creating default empty note
    private var currentNote: Note = Note("",
        "",
        "",
        Calendar.getInstance().time)

    val noteTitle get() = currentNote.title
//    val noteTi


    // Only exposing non-mutable LiveData to view
    private val _lastEditedTimeString: MutableLiveData<String> = MutableLiveData()
    val lastEditedString: LiveData<String> = _lastEditedTimeString

    fun initViewModel(intentNote: String?, locale: Locale, timeZone: TimeZone){
        // Checking if note JSON string in intent exists, if so convert to note object and set to current note
        if (intentNote != null){
            // Convert note json string to note object
            currentNote = NotesUtility.noteFromString(intentNote)

            isEditing = true
        }

        // Init timezone and locale
        this.locale = locale
        this.timeZone = timeZone
    }

    // If isEditing note then edit existing document on firebase by ID else create new document
    fun saveNote() {
        if (isEditing){
            NotesRepository.editNoteOnFirebase(currentNote)
        } else {
            NotesRepository.createNoteOnFirebase(currentNote)
            isEditing = true
        }
    }

    // Deleting note and passing back resulting Task obj to set callback in view
    fun deleteCurrentNote(): Task<Void> {
        return deleteNoteById(currentNote.noteId)
    }

    private fun deleteNoteById(id: String): Task<Void>{
        return NotesRepository.deleteNote(id)
    }

    fun saveNoteToLocalDb(note: Note){
        TODO()
    }

    // Getting formatted time string for passed note
    fun getFormattedDateString(date: Date): String{
        return NotesUtility.formatTimeString(date, locale, timeZone)
    }

    // Getting formatted time string for current note
    fun getThisNoteTimeString(): String{
        return getFormattedDateString(currentNote.lastEdited)
    }
}