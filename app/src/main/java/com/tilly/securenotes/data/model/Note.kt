package com.tilly.securenotes.data.model

import java.util.*

class Note(
    var noteId: String,
    var userId: String,
    var title: String,
    var content: String,
    var lastEdited: Date
) {

}