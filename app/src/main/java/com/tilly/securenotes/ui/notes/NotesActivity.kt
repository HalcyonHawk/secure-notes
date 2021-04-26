package com.tilly.securenotes.ui.notes

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tilly.securenotes.R
import com.tilly.securenotes.databinding.ActivityNotesBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.ui.profile.ProfileActivity

// Notes screen - Display list of notes with buttons to manage the notes.
class NotesActivity : AppCompatActivity() {
    // Create later initialised binding variable for this activity
    private lateinit var binding: ActivityNotesBinding

    // Adapter to fill custom notes list view
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create and set view for activity from binding class
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: NotesViewModel by viewModels()
        // Create and set notes adapter to fill notes list
        notesAdapter = NotesAdapter(arrayListOf())
        binding.notesList.adapter = notesAdapter
        binding.notesList.layoutManager = LinearLayoutManager(this)

        // Set on click listeners for menu items
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.new_note -> {
                    // Start editor activity
                    val intent = Intent(this, EditorActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    // Start profile activity
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Listen for changes in search box text to automatically filter displayed notes items
        binding.searchBox.doOnTextChanged { text, start, before, count ->
            // If search box is empty
            if (text.isNullOrEmpty()){
                // Reset filter and display all notes
                notesAdapter.resetNotes()
            } else {
                // Filter notes
                notesAdapter.filterNotes(text.toString())
            }
        }

        // Set listener for button to alphabetically sort displayed notes
        binding.sortButton.setOnClickListener {
            notesAdapter.sortNotesAlphabetically()
        }


        // Observing for changes to notes list and update adapter for text box auto complete
        viewModel.notesList.observe(this, Observer {notes ->
            // Set adapter for autocomplete search box with newly loaded notes
            val searchAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,
                notes.map { it.title })
            binding.searchBox.setAdapter(searchAdapter)

            notesAdapter.updateFullNotesList(notes)
        })

        // Load notes when activity is created
        refreshNotesList()
    }

    // Refresh notes list from firebase
    private fun refreshNotesList(){
        // Get view model for activity
        val viewModel: NotesViewModel by viewModels()
        // Load notes and display message on success or failure
        viewModel.loadNotes()
            .addOnSuccessListener {
                Toast.makeText(this, "Notes loaded", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Couldn't load notes", Toast.LENGTH_SHORT).show()
            }
    }


    // Refresh notes list when returning to app from another app or activity
    override fun onResume() {
        super.onResume()
        // Get view model for this activity
        val viewModel: NotesViewModel by viewModels()
        // Clear search box after returning to notes activity and load notes
        binding.searchBox.text.clear()
        viewModel.loadNotes()
    }
}