package com.tilly.securenotes.ui.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.tilly.securenotes.R
import com.tilly.securenotes.utilities.InputFocusUtilities
import com.tilly.securenotes.databinding.ActivityEditorBinding
import com.tilly.securenotes.utilities.NotesUtility.observeOnce
import java.lang.ref.WeakReference
import java.util.*

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding
    private lateinit var viewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set custom toolbar view
        setSupportActionBar(binding.topAppBar)
        // Hide toolbar title
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Creating class level viewModel
        viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)


        // Getting note JSON from intent
        val passedNote = intent.extras?.getString("note")
        viewModel.initViewModel(passedNote, resources.configuration.locales[0], TimeZone.getDefault())

        // Initializing text views with title, content and date from current note in viewmodel
        binding.contentTextInput.setText(viewModel.noteContent)
        binding.titleEditText.setText(viewModel.noteTitle)
        binding.dateEdited.text = viewModel.getThisNoteTimeString()

        binding.titleEditText.doAfterTextChanged { editable ->
            viewModel.updateNoteTitle(editable.toString())
        }

        binding.contentTextInput.doAfterTextChanged { editable ->
            viewModel.updateNoteContent(editable.toString())
        }
    }

    // Setting actions for toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Getting viewModel instance

        return when(item.itemId){
            R.id.share -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, viewModel.noteContent)
                    putExtra(Intent.EXTRA_TITLE, viewModel.noteTitle)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                true
            }
            R.id.delete -> {
                // If note deleted successfully, finish activity
                viewModel.deleteCurrentNote().addOnSuccessListener {
                    finish()
                    Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.submit -> {
                // If title field is empty then show error message and focus title field
                if (binding.titleEditText.text.isNullOrBlank()){
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    binding.titleEditText.requestFocusFromTouch()
                } else {
                    // Hide keyboard, unfocus edittext view and submit current note to firebase
                    InputFocusUtilities.stopEditingText(context = this, currentFocus = currentFocus)
                    // Save note when pressing submit button. if note saved successfully then change last edited date or show error message
                    viewModel.saveNote().observeOnce(Observer {
                        if (it){
                            binding.dateEdited.text = viewModel.getThisNoteTimeString()
                        } else {
                            Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                true
            }
            android.R.id.home -> {
                // Pressing back arrow button on toolbar finishes activity

                // If title field is empty then show error message and focus title field
                if (binding.titleEditText.text.isBlank() && binding.contentTextInput.text.isNotBlank()){
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    binding.titleEditText.requestFocusFromTouch()
                } else {
                    // Hide keyboard, unfocus edittext view and submit current note to firebase
                    InputFocusUtilities.stopEditingText(context = this, currentFocus = currentFocus)
                    if (binding.titleEditText.text.isNotBlank()){
                        viewModel.saveNote()
                    }
                    val returnIntent = Intent()
                    setResult(Activity.RESULT_CANCELED, returnIntent)
                    finish()
                }
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }



    // Initialising toolbar buttons and observing if an edit text view is focused to change toolbar button visibility
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)

        val shareMenuItem = menu?.findItem(R.id.share)
        val deleteMenuItem = menu?.findItem(R.id.delete)
        val submitMenuItem = menu?.findItem(R.id.submit)


        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(submitMenuItem, deleteMenuItem, shareMenuItem)

        // Changing toolbar button visibility if focused
        binding.titleEditText.onFocusChangeListener = editTextFocusListener
        binding.contentTextInput.onFocusChangeListener = editTextFocusListener

        // After views have been initialized, if the note is new then focus title field and show keyboard
        if (viewModel.isNoteNew){
            InputFocusUtilities.startEditingTextField(WeakReference(this), binding.titleEditText)
        }

        return super.onCreateOptionsMenu(menu)
    }
}