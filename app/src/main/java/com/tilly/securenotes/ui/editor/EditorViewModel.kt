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
    // TODO: Update name to isNew

    // Declare timezone and locale for formatting date and time
    private lateinit var locale: Locale

    private lateinit var timeZone: TimeZone
    // Creating default empty note
    private var currentNote: Note = Note("",
        "",
        "",
        Calendar.getInstance().time)

    // LiveData to if any text input view is focused and alert view to appropriately update the view
    private val _isTextInputFocused: MutableLiveData<Boolean> = MutableLiveData()
    val isTextInputFocused: LiveData<Boolean>
        get() = _isTextInputFocused

    // Getters to only expose required Note object properties
    // TODO use getters and setters?
    val noteTitle get() = currentNote.title
    val noteContent get() = currentNote.content
    val isNoteNew get() = currentNote.noteId.isNotBlank()

    val updateNoteTitle = { title: String ->
        // TODO: Sbmit to db periodially?
        this.currentNote.title = title
    }

    val updateNoteContent = { content: String ->
        this.currentNote.content = content
    }

    // Only exposing non-mutable LiveData to view using getter
    private val _lastEditedTimeString: MutableLiveData<String> = MutableLiveData()
    val lastEditedString: LiveData<String>
        get() = _lastEditedTimeString

    fun initViewModel(intentNote: String?, locale: Locale, timeZone: TimeZone){
        // Checking if note JSON string in intent exists, if so convert to note object and set to current note
        if (intentNote != null){
            // Convert note json string to note object
            currentNote = NotesUtility.noteFromString(intentNote)
        }

        // Init timezone and locale
        this.locale = locale
        this.timeZone = timeZone
    }

    // Setter to post new edit text state to observers
    fun setTextEditHasFocus(isEditing: Boolean){
        _isTextInputFocused.postValue(isEditing)
    }

    fun updateEditedNote(){

    }

    // Function for checking if notes have been edited or are the same
    fun haveNotesBeenEdited(oldNote: Note, newNote: Note): Boolean{
        return oldNote != newNote
    }

    // If isEditing note then edit existing document on firebase by ID else create new document
    fun saveNote() {
        if (currentNote.noteId.isNotBlank()){
            NotesRepository.editNoteOnFirebase(currentNote)
        } else {
            currentNote.noteId = NotesRepository.createNoteOnFirebase(currentNote)

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