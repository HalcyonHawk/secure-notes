package com.tilly.securenotes.data.model

import com.google.type.DateTime
import java.util.*

data class Note( var noteId: String,
    var title: String,
    var content: String,
    var lastEdited: Date
)