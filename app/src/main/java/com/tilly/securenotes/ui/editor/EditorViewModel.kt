package com.tilly.securenotes.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.tilly.securenotes.data.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.utilities.NotesUtilities
import java.util.*
// ViewModel for editor activity - Store details of note being edited and update note in database
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

    // Get only the required Note object properties
    val noteTitle get() = currentNote.title
    val noteContent get() = currentNote.content
    val isNoteNew get() = currentNote.noteId.isBlank()
    val isCurrentNoteFav get() = currentNote.favourite

    // LiveData representing favourite state of the note
    val noteFavState: LiveData<Boolean> get() = _noteFavState

    // Set note title
    val updateNoteTitle = { title: String ->
        this.currentNote.title = title
    }
    // Set note content
    val updateNoteContent = { content: String ->
        this.currentNote.content = content
    }

    // Initialise view model with the note being edited or a new note if intentNote is null
    fun initViewModel(intentNote: String?, locale: Locale, timeZone: TimeZone){
        // Check if note JSON string intent exists
        if (intentNote != null){
            // Convert note json string to note object
            currentNote = NotesUtilities.noteFromString(intentNote)
        }

        _noteFavState.postValue(currentNote.favourite)

        // Initialise timezone and locale for formatting last edited date and time string
        this.locale = locale
        this.timeZone = timeZone
    }

    // Toggle favourite state of the note and update the database
    fun toggleFavouriteNote(): Task<Void>{
        val newFavState = !currentNote.favourite
        return NoteRepository.favouriteNote(currentNote.noteId, newFavState)
            .addOnSuccessListener {
                currentNote.favourite = newFavState
                // Update livedata with new favourite state to update favourite icon
                _noteFavState.postValue(newFavState)
            }
    }

    // Save note on firebase
    fun saveNote(): LiveData<Boolean> {
        val success = MutableLiveData<Boolean>()
        currentNote.lastEdited = Calendar.getInstance().time
        // If document ID given, update existing note on firebase with given ID
        if (!isNoteNew){
            NoteRepository.editNoteOnFirebase(currentNote)
                .addOnCompleteListener {
                    success.postValue(it.isSuccessful)
                }
        // Else create a new note document
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

    // Delete note and return Task object to set callback in view
    fun deleteCurrentNote(): Task<Void> {
        return deleteNoteById(currentNote.noteId)
    }

    // Delete a note from the database using note ID given
    private fun deleteNoteById(id: String): Task<Void>{
        return NoteRepository.deleteNote(id)
    }


    // Get formatted time string for date given
    private fun getFormattedDateString(date: Date): String{
        return NotesUtilities.formatTimeString(date, locale, timeZone)
    }

    // Get formatted time string for current note
    fun getThisNoteTimeString(): String{
        return getFormattedDateString(currentNote.lastEdited)
    }
}