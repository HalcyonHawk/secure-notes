package com.tilly.securenotes.ui.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.tilly.securenotes.R
import com.tilly.securenotes.utilities.InputFocusUtilities
import com.tilly.securenotes.databinding.ActivityEditorBinding
import com.tilly.securenotes.utilities.NotesUtilities
import com.tilly.securenotes.utilities.NotesUtilities.observeOnce
import java.lang.ref.WeakReference
import java.util.*

// Create/edit note screen - Allow user to either create a new note or edit an existing note
class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding
    private lateinit var viewModel: EditorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create activity view from binding class
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set custom toolbar view
        setSupportActionBar(binding.topAppBar)
        // Hide toolbar title and show back button
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Creating class viewModel
        viewModel = ViewModelProvider(this).get(EditorViewModel::class.java)

        // Getting note JSON from intent and initialising ViewModel with it
        val passedNote = intent.extras?.getString("note")
        viewModel.initViewModel(passedNote, resources.configuration.locales[0], TimeZone.getDefault())

        // Initializing text views with title, content and date from current note in viewmodel
        binding.contentTextInput.setText(viewModel.noteContent)
        binding.titleEditText.setText(viewModel.noteTitle)
        binding.dateEdited.text = viewModel.getThisNoteTimeString()

        // Submit note changes after user is done editing them
        binding.titleEditText.doAfterTextChanged { editable ->
            viewModel.updateNoteTitle(editable.toString())
        }

        binding.contentTextInput.doAfterTextChanged { editable ->
            viewModel.updateNoteContent(editable.toString())
        }
    }

    // Setting actions for toolbar buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId){
            R.id.favourite -> {
                // Favourite note when favourite button (star icon) is pressed in toolbar
                viewModel.toggleFavouriteNote()
                    .addOnFailureListener {
                    Toast.makeText(this, "Failed to favourite note", Toast.LENGTH_SHORT).show()
                }.addOnSuccessListener {
                    // Show toast message depending on if note is favourited or not
                    if(viewModel.isCurrentNoteFav)
                        Toast.makeText(this, "Note favourited" , Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, "Note unfavourited" , Toast.LENGTH_SHORT).show()
                    }
                true
            }
            R.id.share -> {
                // Create and launch share intent with note title and content
                startActivity(NotesUtilities.createShareIntent(viewModel.noteTitle, viewModel.noteContent))
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
                    // Save note when submit button pressed.
                    // If note saved successfully then change last edited date or show error message
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
            // Pressing back arrow button on toolbar finishes activity
            android.R.id.home -> {
                // If title field is empty, then show error message and focus title field
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

    // Set favourite icon for toolbar button
    private fun setFavMenuItemIcon(menuItem: MenuItem?, favourite: Boolean){
        // If current note is favourite, then set toolbar button icon to appropriate image
        if (favourite){
            menuItem?.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_star_white_24dp, null)
        } else {
            menuItem?.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_star_border_white_24dp, null)
        }
    }

    // Initialising toolbar buttons and observing if an EditText view is focused to change toolbar button visibility
    // Buttons such as the favourite button should only be displayed when note is not being edited
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)

        // Get menu item references from ID
        val favMenuItem = menu?.findItem(R.id.favourite)
        val shareMenuItem = menu?.findItem(R.id.share)
        val deleteMenuItem = menu?.findItem(R.id.delete)
        val submitMenuItem = menu?.findItem(R.id.submit)


        // Set text focus listener on EditText view to handle changing button visibility
        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(submitMenuItem, deleteMenuItem,
            shareMenuItem, favMenuItem)

        // Change toolbar button visibility if focused
        binding.titleEditText.onFocusChangeListener = editTextFocusListener
        binding.contentTextInput.onFocusChangeListener = editTextFocusListener

        // Set favourite icon for the menu button by notes is_favourite data
        setFavMenuItemIcon(favMenuItem, viewModel.noteFavState.value!!)

        // Observe for favourite state changes
        viewModel.noteFavState.observe(this, Observer { favState ->
            setFavMenuItemIcon(favMenuItem, favState)
        })

        // After views have been initialized, if the note is new then focus title field and show keyboard
        if (viewModel.isNoteNew){
            InputFocusUtilities.startEditingTextField(WeakReference(this), binding.titleEditText)
        }

        return super.onCreateOptionsMenu(menu)
    }
}