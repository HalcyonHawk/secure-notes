package com.tilly.securenotes.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.tilly.securenotes.data.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.utilities.NotesUtility
import java.util.*
// ViewModel for editor activity, stores details of currently edited note and contains functions for updating note
// in the database.
class EditorViewModel: ViewModel() {
    // Declare timezone and locale for formatting date and time
    private lateinit var locale: Locale

    private lateinit var timeZone: TimeZone
    // Creating default empty note
    private var currentNote: Note = Note("",
        "",
        "",
        Calendar.getInstance().time)

    private val _noteFavState = MutableLiveData<Boolean>(false)

    // Getters to only expose required Note object properties
    val noteTitle get() = currentNote.title
    val noteContent get() = currentNote.content
    val isNoteNew get() = currentNote.noteId.isBlank()
    val isCurrentNoteFav get() = currentNote.favourite

    // LiveData representing favourite state of the note
    val noteFavState: LiveData<Boolean> get() = _noteFavState

    // Setters for note title and content
    val updateNoteTitle = { title: String ->
        this.currentNote.title = title
    }

    val updateNoteContent = { content: String ->
        this.currentNote.content = content
    }

    // Function initialising view model with the currently edited note or a new note if intentNote is null
    fun initViewModel(intentNote: String?, locale: Locale, timeZone: TimeZone){
        // Checking if note JSON string in intent exists, if so convert to note object and set to current note
        if (intentNote != null){
            // Convert note json string to note object
            currentNote = NotesUtility.noteFromString(intentNote)
        }

        _noteFavState.postValue(currentNote.favourite)

        // Init timezone and locale for formatting last edited date and time string
        this.locale = locale
        this.timeZone = timeZone
    }

    // Toggle favourite state of the current note and update the database
    fun toggleFavouriteNote(): Task<Void>{
        val newFavState = !currentNote.favourite
        return NoteRepository.favouriteNote(currentNote.noteId, newFavState)
            .addOnSuccessListener {
                currentNote.favourite = newFavState
                // Update livedata with new favourite state to update favourite icon
                _noteFavState.postValue(newFavState)
            }
    }

    // Function to save note state on firebase
    // If editing existing note then edit it's document on firebase by ID or else create a new note document
    fun saveNote(): LiveData<Boolean> {
        val success = MutableLiveData<Boolean>()
        currentNote.lastEdited = Calendar.getInstance().time
        if (!isNoteNew){
            NoteRepository.editNoteOnFirebase(currentNote)
                .addOnCompleteListener {
                    success.postValue(it.isSuccessful)
                }
        } else {
            NoteRepository.createNoteOnFirebase(currentNote)
                .addOnSuccessListener { documentReference ->
                    currentNote.noteId = documentReference.id
                }
                .addOnCompleteListener{
                    success.postValue(it.isSuccessful)
                }
        }
        return success
    }

    // Deleting note and passing back resulting Task obj to set callback in view
    fun deleteCurrentNote(): Task<Void> {
        return deleteNoteById(currentNote.noteId)
    }

    // Delete a note from the database based on the passed note ID
    private fun deleteNoteById(id: String): Task<Void>{
        return NoteRepository.deleteNote(id)
    }


    // Getting formatted time string for passed date
    private fun getFormattedDateString(date: Date): String{
        return NotesUtility.formatTimeString(date, locale, timeZone)
    }

    // Getting formatted time string for current note
    fun getThisNoteTimeString(): String{
        return getFormattedDateString(currentNote.lastEdited)
    }
}