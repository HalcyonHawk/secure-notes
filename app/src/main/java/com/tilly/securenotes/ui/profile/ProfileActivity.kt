package com.tilly.securenotes.ui.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import com.squareup.picasso.Picasso
import com.tilly.securenotes.R
import com.tilly.securenotes.databinding.ActivityProfileBinding
import com.tilly.securenotes.utilities.InputFocusUtilities
import com.tilly.securenotes.utilities.NotesUtility
import com.tilly.securenotes.utilities.NotesUtility.observeOnce

// Profile activity allowing user to change account name, email, password and profile picture
class ProfileActivity : AppCompatActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    lateinit var binding: ActivityProfileBinding
    lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Creating activity layout using binding class
        binding = ActivityProfileBinding.inflate(layoutInflater)
        // Setting activity view to root of
        setContentView(binding.root)
        // Set custom toolbar view as action bar
        setSupportActionBar(binding.topAppBar)
        // Show back button in toolbar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Getting viewmodel for this activity
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Set clicking profile image to open image picker from image picker library
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

        // When profile picture URI is changed, load new profile picture into ImageView if not null, else set to default picture
        viewModel.profilePicUri.observe(this, Observer { uri ->
            if (uri != null) {
                Picasso.get().load(uri).into(binding.profileImage)
            } else {
                binding.profileImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_account_box_black_24dp, null))
            }
        })

        // Register listener for auth state change to go back to login when user has been logged out
        viewModel.registerAuthListener(FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null) {
                goBackToLogin()
            }
        })

        // Click listener for submit new password button
        binding.submitPass.setOnClickListener {
            // If new passwords match and new password is longer than 6 characters then change pass in database and show success message, otherwise show an error
            if (binding.newPassword.text.toString() == binding.confirmNewPassword.text.toString()) {
                viewModel.changePassword(binding.newPassword.text.toString())
                        // Handle result of changing password by showing success message or error
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Password change failed", Toast.LENGTH_SHORT).show()
                        }

                    }
            } else if (binding.newPassword.text.length < 6) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters long",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }

        }
        // Initialise text fields with current user's username and email
        binding.username.setText(viewModel.userName)
        binding.email.setText(viewModel.userEmail)

        // Load profile picture from firebase auth
        viewModel.loadProfilePicture()
    }

    // Close all activities and return to LoginActivity
    private fun goBackToLogin(){
        startActivity(NotesUtility.createGoToLoginIntent(this.baseContext))
    }

    // When images have been selected using image picker, update profile picture in firebase auth
    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.updateProfilePicture(uris.first().normalizeScheme())
    }

    // Inflate options menu and observe if name or email are being edited to show/hide toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)

        // Getting toolbar menu items from item IDs
        val submitItem = menu?.findItem(R.id.submit_profile)
        val deleteAccountItem = menu?.findItem(R.id.delete_account)
        val logoutItem = menu?.findItem(R.id.logout)

        // Listener for showing/hiding menu items depending on if a text box is focused or not
        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(
            submitItem,
            deleteAccountItem,
            logoutItem)

        // Changing toolbar button visibility if focused
        binding.username.onFocusChangeListener = editTextFocusListener
        binding.email.onFocusChangeListener = editTextFocusListener

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handling actions for each toolbar menu item
        when (item.itemId) {
            R.id.submit_profile -> {
                // Submit button commits changes to user to firebase auth
                viewModel.commitChangedUser(
                    binding.username.text.toString(),
                    binding.email.text.toString()
                ).observeOnce(Observer {
                    // Show appropriate message to user depending on result of profile changes
                    if (it == null) {
                        Toast.makeText(this, "No changes", Toast.LENGTH_SHORT).show()
                    } else if (it) {
                        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.delete_account -> {
                // Show confirmation dialog before deleting the user's account
                showDeleteUserDialog()
            }
            R.id.logout -> {
                // Logout user from pp
                viewModel.logout()
            }
            android.R.id.home -> {
                // Close activity if back pressed
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Creating confirmation dialog allowing the user to delete their account
    private fun showDeleteUserDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Would you like to delete your account?")

        // Listener for positive and negative action buttons.
        // Yes button deletes account from firebase and shows message on success/failure. No button closes dialog.
        builder.setPositiveButton("Yes") { dialog, which ->
            viewModel.deleteUser()
                .addOnSuccessListener {
                    goBackToLogin()
                    Toast.makeText(this, "Your account has been deleted", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    if (it is FirebaseAuthRecentLoginRequiredException){
                        Toast.makeText(this, "Logout and try again to delete account", Toast.LENGTH_LONG).show()
                    }
                }
        }
        builder.setNegativeButton("No", null)

        // Create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}