package com.tilly.securenotes.data.model

import java.util.*

// Data class modeling a note object stored in the firebase database
data class Note( var noteId: String,
    var title: String,
    var content: String,
    var lastEdited: Date,
    var favorite: Boolean = false
) {
    // Overriding equals function for comparing if note objects are different
    override fun equals(other: Any?): Boolean {
        val newNote = other as Note
        return this.title != newNote.title || this.content != newNote.content
    }

    // Overriding equals() requires hashCode() to be overriden too
    override fun hashCode(): Int {
        var result = noteId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + lastEdited.hashCode()
        return result
    }
}