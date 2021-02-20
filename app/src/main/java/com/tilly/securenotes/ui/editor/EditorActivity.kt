package com.tilly.securenotes.ui.editor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import com.tilly.securenotes.R
import com.tilly.securenotes.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)

        val viewModel: EditorViewModel by viewModels()

        binding.topAppBar.setOnMenuItemClickListener { item ->
//            when(item.id)
        }


    }

    // Initialising toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_menu, menu)
        val shareMenuItem = menu?.findItem(R.id.share)
        val deleteMenuItem = menu?.findItem(R.id.delete)
        val submitMenuItem = menu?.findItem(R.id.submit)

        // Changing toolbar buttons if focused
        binding.textInput.setOnFocusChangeListener { v, hasFocus ->
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



        return true
    }
}