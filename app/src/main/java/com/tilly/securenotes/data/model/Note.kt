package com.tilly.securenotes.data.model

import com.google.type.DateTime
import java.util.*

data class Note( var noteId: String,
    var title: String,
    var content: String,
    var lastEdited: Date
) {
    // Overriding equals function for comparing if notes have been edited
    override fun equals(other: Any?): Boolean {
        val newNote = other as Note
        // TODO: Include lastediteddate in comparison?
        return this.title != newNote.title || this.content != newNote.content
    }
}