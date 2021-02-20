package com.tilly.securenotes.ui.notes

import com.google.gson.Gson
import com.tilly.securenotes.data.model.Note
import java.text.SimpleDateFormat
import java.util.*

// Notes utility functions
object NotesUtility {
    // Creating instance of Gson to convert objects to JSON string
    private val gson = Gson()

    // Converting note to string for passing to edit activity
    fun noteToString(note: Note): String{
        return gson.toJson(note)
    }

    // Convert note in json form into note object
    fun noteFromString(json: String): Note{
        return gson.fromJson(json, Note::class.java)
    }

    // Format date function variation  taking note object instead of Date
    fun formatTimeString(note: Note, locale: Locale, timeZone: TimeZone): String {
        return formatTimeString(note.lastEdited, locale, timeZone)
    }

    // Format date as hour:minute day month year and format with given locale
    fun formatTimeString(date: Date, locale: Locale, timeZone: TimeZone): String {
        val formatter = SimpleDateFormat("h:mm a dd MMMM yyyy", locale)
        formatter.timeZone = timeZone
        return formatter.format(date)
    }
}