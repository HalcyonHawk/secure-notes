package com.tilly.securenotes.ui.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.databinding.ActivityNotesBinding
import com.tilly.securenotes.ui.editor.EditorActivity
import com.tilly.securenotes.ui.profile.ProfileActivity

class NotesActivity : AppCompatActivity() {

    // Lateinit is a variable that cant be null but is initialized later
    private lateinit var binding: ActivityNotesBinding

    // Adapter to fill custom notes list view
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: NotesViewModel by viewModels()
        notesAdapter = NotesAdapter(arrayListOf())

        binding.notesList.adapter = notesAdapter
        binding.notesList.layoutManager = LinearLayoutManager(this)

        //TODO: Swipe list menu item

        viewModel.notesList.observe(this, Observer { notes ->
            notesAdapter.updateNotes(notes)
        })

        refreshNotesList()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.new_note -> {
                    // Start activity for result
                    val intent = Intent(this, EditorActivity::class.java)
                    startActivity(intent)

                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    fun refreshNotesList(){
        val viewModel: NotesViewModel by viewModels()

        viewModel.loadNotes().observe(this, Observer { resultStatusWrapper ->
            when (resultStatusWrapper) {
                is ResultStatusWrapper.Success -> {
                    Toast.makeText(
                        this,
                        "Notes loaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ResultStatusWrapper.Error -> {
                    throw resultStatusWrapper.exception
                }
                else -> {
                }
            }
        })
    }


    // Refresh notes list when returning to app from another app or activity
    override fun onResume() {
        super.onResume()
        val viewModel: NotesViewModel by viewModels()
        viewModel.loadNotes()
    }


}