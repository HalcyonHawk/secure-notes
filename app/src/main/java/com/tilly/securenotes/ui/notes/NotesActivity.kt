package com.tilly.securenotes.ui.notes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.tilly.securenotes.R
import com.tilly.securenotes.databinding.ActivityNotesBinding
import com.tilly.securenotes.ui.editor.EditorActivity

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

        viewModel.notesList.observe(this, Observer { notes ->
            notesAdapter.updateNotes(notes)
        })

        viewModel.loadNotes()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.new_note -> {
                    startActivity(Intent(this, EditorActivity::class.java))

                    true
                }
                R.id.profile -> {

                    true
                }
                else -> false
            }
        }

    }


    // Refresh notes list when returning to app
    override fun onResume() {
        super.onResume()
        val viewModel: NotesViewModel by viewModels()
        viewModel.loadNotes()
    }

}