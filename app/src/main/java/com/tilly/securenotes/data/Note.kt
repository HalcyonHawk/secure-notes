package com.tilly.securenotes.data

import java.util.*

// Note data class - Models a note object stored in the firebase database
data class Note( var noteId: String,
    var title: String,
    var content: String,
    var lastEdited: Date,
    var favourite: Boolean = false
) {
    // Overriding equals function for comparing if note objects are different
    override fun equals(other: Any?): Boolean {
        val newNote = other as Note
        // Compare notes by title and content
        return this.title != newNote.title || this.content != newNote.content
    }

    // Overriding equals() requires hashCode() to be overridden too
    override fun hashCode(): Int {
        var result = noteId.hashCode()
        result += title.hashCode()
        result += content.hashCode()
        result += lastEdited.hashCode()
        return result
    }
}