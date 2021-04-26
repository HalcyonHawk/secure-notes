package com.tilly.securenotes.utilities


import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.tilly.securenotes.data.Note
import com.tilly.securenotes.ui.login.LoginActivity
import java.text.SimpleDateFormat
import java.util.*


// Utility class with functions for handling notes objects
object NotesUtilities {
    // Create instance of Gson to convert objects to JSON string
    private val gson = Gson()

    // Converting note object to string for passing to edit activity
    fun noteToString(note: Note): String{
        return gson.toJson(note)
    }

    // Convert note in json into note object
    fun noteFromString(json: String): Note {
        return gson.fromJson(json, Note::class.java)
    }

    // Format date function variation taking note object instead of Date
    fun formatTimeString(note: Note, locale: Locale, timeZone: TimeZone): String {
        return formatTimeString(
            note.lastEdited,
            locale,
            timeZone
        )
    }

    // Format date as hour:minute am/pm day month year and format with given locale
    fun formatTimeString(date: Date, locale: Locale, timeZone: TimeZone): String {
        val formatter = SimpleDateFormat("h:mm a dd MMMM yyyy", locale)
        formatter.timeZone = timeZone
        return formatter.format(date)
    }

    // Extension function to observe live data then remove observer after executing onChanged once
    fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observeForever(object : Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        })
    }

    // Utility function to create a share intent which opens share menu to share a note title and text
    fun createShareIntent(noteTitle: String, noteContent: String): Intent{
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, noteContent)
            putExtra(Intent.EXTRA_TITLE, noteTitle)
            type = "text/plain"
        }

        return Intent.createChooser(sendIntent, null)
    }

    // Create an intent that will close all activities and start the LoginActivity
    fun createGoToLoginIntent(context: Context): Intent{
        val i = Intent(context, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }
}