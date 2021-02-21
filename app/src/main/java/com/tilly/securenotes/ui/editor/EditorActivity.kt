package com.tilly.securenotes.ui.editor

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.tilly.securenotes.R
import com.tilly.securenotes.databinding.ActivityEditorBinding
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
        binding.contentTextInput.setText(viewModel.noteContent)
        binding.titleEditText.setText(viewModel.noteTitle)
        binding.dateEdited.text = viewModel.getThisNoteTimeString()

        //TODO: Periodically update text on server as typing
        binding.titleEditText.doOnTextChanged { text, start, before, count ->
            viewModel.updateNoteTitle(text.toString())
        }
        binding.contentTextInput.doOnTextChanged { text, start, before, count ->
            viewModel.updateNoteContent(text.toString())
        }

    }

    override fun onResume() {
        super.onResume()
        val viewModel: EditorViewModel by viewModels()

        // If new note then automatically focus title field
        if (viewModel.isNoteNew){
            initFocusTitleField()
            viewModel.setTextEditHasFocus(true)
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

    private fun initFocusTitleField(){
        binding.titleEditText.requestFocusFromTouch()
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.titleEditText, InputMethodManager.SHOW_IMPLICIT)

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
                // If title field is empty then show error message and focus title field
                if (binding.titleEditText.text.isNullOrBlank()){
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                    binding.titleEditText.requestFocusFromTouch()
                } else {
                    // Hide keyboard, unfocus edittext view and submit current note to firebase
                    stopEditingText()
                    viewModel.saveNote()
                }
                true
            }
            android.R.id.home -> {
                // Pressing back arrow button on toolbar finishes activity
                stopEditingText()
                viewModel.saveNote()
                // TODO: Commit note onCreate then compare euality, if diff then noteactivity needs to update
                val returnIntent = Intent()
                setResult(Activity.RESULT_CANCELED, returnIntent)
                finish()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }


    // Initialising toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        val viewModel: EditorViewModel by viewModels()

        val shareMenuItem = menu?.findItem(R.id.share)
        val deleteMenuItem = menu?.findItem(R.id.delete)
        val submitMenuItem = menu?.findItem(R.id.submit)

        // Lambda to update fo
        val updateVisibleToolbarButtons = { hasFocus: Boolean ->
            submitMenuItem?.isVisible = hasFocus
            deleteMenuItem?.isVisible = !hasFocus
            shareMenuItem?.isVisible = !hasFocus

        }

        viewModel.isTextInputFocused.observe(this, Observer { isFocused ->
            updateVisibleToolbarButtons(isFocused)
        })

        val editTextFocusListener = {view: View?, hasFocus: Boolean ->
            viewModel.setTextEditHasFocus(hasFocus)
        }

        // Changing toolbar button visibility if focused
        binding.titleEditText.setOnFocusChangeListener(editTextFocusListener)
        binding.contentTextInput.setOnFocusChangeListener(editTextFocusListener)



        return super.onCreateOptionsMenu(menu)
    }
}