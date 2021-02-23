package com.tilly.securenotes.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.utilities.NotesUtility
import java.util.*

class EditorViewModel: ViewModel() {
    // Declare timezone and locale for formatting date and time
    private lateinit var locale: Locale

    private lateinit var timeZone: TimeZone
    // Creating default empty note
    private var currentNote: Note = Note("",
        "",
        "",
        Calendar.getInstance().time)



    // Getters to only expose required Note object properties
    // TODO use getters and setters?
    val noteTitle get() = currentNote.title
    val noteContent get() = currentNote.content
    val isNoteNew get() = currentNote.noteId.isBlank()

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


    // If isEditing note then edit existing document on firebase by ID else create new document
    fun saveNote() {

        if (!isNoteNew){
            NoteRepository.editNoteOnFirebase(currentNote)
        } else {
            currentNote.noteId = NoteRepository.createNoteOnFirebase(currentNote)

        }
    }

    // Deleting note and passing back resulting Task obj to set callback in view
    fun deleteCurrentNote(): Task<Void> {
        return deleteNoteById(currentNote.noteId)
    }

    private fun deleteNoteById(id: String): Task<Void>{
        return NoteRepository.deleteNote(id)
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