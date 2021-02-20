package com.tilly.securenotes.ui.editor

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.Note
import com.tilly.securenotes.databinding.ActivityEditorBinding
import com.tilly.securenotes.ui.notes.NotesUtility
import java.util.*

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding

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

        val viewModel: EditorViewModel by viewModels()

        // Getting note JSON from intent
        val passedNote = intent.extras?.getString("note")
        viewModel.initViewModel(passedNote, resources.configuration.locales[0], TimeZone.getDefault())

        // Initializing text views with title, content and date from current note in viewmodel
        binding.contentTextInput.setText(viewModel.currentNote.content)
        binding.titleEditText.setText(viewModel.currentNote.title)
        binding.dateEdited.text = viewModel.getThisNoteTimeString()

        binding.titleEditText.addTextChangedListener{ text ->
            viewModel.

        }
    }

    // Hide keyboard and unfocus edittext view
    private fun stopEditingText(){
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var focusedView = currentFocus

        if (focusedView == null){
            focusedView = View(this)
        }

        inputMethodManager.hideSoftInputFromWindow(focusedView.windowToken, 0)
        focusedView.clearFocus()
    }

    // Setting actions for toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Getting viewModel instance
        val viewModel: EditorViewModel by viewModels()

        return when(item.itemId){
            R.id.share -> {
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.delete -> {
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_SHORT).show()
                // If note deleted successfully, finish activity
                viewModel.deleteCurrentNote().addOnSuccessListener {
                    finish()
                }
                true
            }
            R.id.submit -> {
                // Hide keyboard, unfocus edittext view and submit current note to firebase
                stopEditingText()
                viewModel.saveNote()
                true
            }
            android.R.id.home -> {
                // Pressing back arrow button on toolbar calls same function as navigation back button
                onBackPressed()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }



    // Initialising toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        val shareMenuItem = menu?.findItem(R.id.share)
        val deleteMenuItem = menu?.findItem(R.id.delete)
        val submitMenuItem = menu?.findItem(R.id.submit)


        // Changing toolbar button visibility if focused
        binding.contentTextInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                submitMenuItem?.isVisible = true

                deleteMenuItem?.isVisible = false
                shareMenuItem?.isVisible = false
            } else {
                submitMenuItem?.isVisible = false

                deleteMenuItem?.isVisible = true
                shareMenuItem?.isVisible = true
            }
        }

        return super.onCreateOptionsMenu(menu)
    }
}