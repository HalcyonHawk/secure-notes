package com.tilly.securenotes.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import com.squareup.picasso.Picasso
import com.tilly.securenotes.R
import com.tilly.securenotes.utilities.InputFocusUtilities
import com.tilly.securenotes.data.model.User
import com.tilly.securenotes.databinding.ActivityProfileBinding
import com.tilly.securenotes.ui.login.LoginActivity
import com.tilly.securenotes.utilities.NotesUtility.observeOnce

class ProfileActivity : AppCompatActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    lateinit var binding: ActivityProfileBinding
    lateinit var viewModel: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflating activity layout using binding class to access views without findViewById()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        // Setting activity view to root of
        setContentView(binding.root)
        // Set custom toolbar view as action bar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)


        // Check if currently logged in firebase authentication user is found in
        // TODO: More to init{} in viewmodel?
//        viewModel.initCurrentUser().observe(this, Observer { isUserFound ->
//            if (isUserFound) {
//                initViews(viewModel.user)
//            } else {
//                Toast.makeText(this, "Current user not found", Toast.LENGTH_SHORT).show()
//            }
//        })

        // Initialise text fields
        binding.username.setText(viewModel.userName)
        binding.email.setText(viewModel.userEmail)

        // Set click profile image to open image picker
        binding.profileImage.setOnClickListener {
            BottomSheetImagePicker.Builder(getString(R.string.file_provider))
                .cameraButton(ButtonType.Tile)
                .galleryButton(ButtonType.Button)
                .singleSelectTitle(R.string.select_new_image)
                .peekHeight(R.dimen.peekHeight)
                .columnSize(R.dimen.columnSize)
                .requestTag("single")
                .show(supportFragmentManager)
        }

        viewModel.profilePicUri.observe(this, Observer { uri ->
            if (uri != null){
                Picasso.get().load(uri).into(binding.profileImage)
            } else {
                Toast.makeText(this, "Failed to load new profile picture", Toast.LENGTH_SHORT).show()
            }
        })

        // Register listener for auth state change
        viewModel.registerAuthListener(FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null){
                val i = Intent(this, LoginActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(i)
            }
        })
    }


    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.updateProfilePicture(uris.first().normalizeScheme())
    }

    // Inflate options menu and observe if name or email are being edited to show/hide toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)


        val submitItem = menu?.findItem(R.id.submit_profile)
        val deleteAccountItem = menu?.findItem(R.id.delete_account)
        val logoutItem = menu?.findItem(R.id.logout)

        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(submitItem, deleteAccountItem, logoutItem)

        // Changing toolbar button visibility if focused
        binding.username.onFocusChangeListener = editTextFocusListener
        binding.email.onFocusChangeListener = editTextFocusListener

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.submit_profile -> {
                viewModel.commitChangedUser(binding.username.text.toString(),
                    binding.email.text.toString()).observeOnce(Observer {
                    if (it == null){
                        Toast.makeText(this, "No changes", Toast.LENGTH_SHORT).show()
                    } else if(it){
                        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.delete_account -> {}
            R.id.logout -> {
                viewModel.logout()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}