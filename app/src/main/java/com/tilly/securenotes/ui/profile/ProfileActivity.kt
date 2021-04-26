package com.tilly.securenotes.ui.profile

import android.net.Uri
import android.os.Bundle
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
import com.tilly.securenotes.utilities.NotesUtilities
import com.tilly.securenotes.utilities.NotesUtilities.observeOnce

// Profile screen - Allows user to view and change account details. This includes things such as
// change account name, email, password and profile picture
class ProfileActivity : AppCompatActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    lateinit var binding: ActivityProfileBinding
    lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create activity layout using binding class
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set custom toolbar view as action bar
        setSupportActionBar(binding.topAppBar)
        // Show back button in toolbar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Get viewmodel for this activity
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        // Pressing profile image opens image picker from image picker library
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

        // When profile picture URI is changed
        viewModel.profilePicUri.observe(this, Observer { uri ->
            // If profile picture URI is not null (has been changed)
            if (uri != null) {
                // Load new profile picture into ImageView
                Picasso.get().load(uri).into(binding.profileImage)
            } else {
                // Set to default picture
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
            // If new passwords match and new password is longer than 6 characters
            if (binding.newPassword.text.toString() == binding.confirmNewPassword.text.toString()) {
                //Update password in database
                viewModel.changePassword(binding.newPassword.text.toString())
                    // Handle result of changing password by showing success message or error
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Password change failed", Toast.LENGTH_SHORT).show()
                        }

                    }
            // Password must be longer than 6 characters
            } else if (binding.newPassword.text.length < 6) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters long",
                    Toast.LENGTH_SHORT
                ).show()
            // New password and confirm new password input must match
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
        startActivity(NotesUtilities.createGoToLoginIntent(this.baseContext))
    }

    // When images have been selected using image picker, update profile picture in firebase auth
    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        viewModel.updateProfilePicture(uris.first().normalizeScheme())
    }

    // Inflate options menu and observe if name or email are being edited to show/hide toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)

        // Get toolbar menu items from item IDs
        val submitItem = menu?.findItem(R.id.submit_profile)
        val deleteAccountItem = menu?.findItem(R.id.delete_account)
        val logoutItem = menu?.findItem(R.id.logout)

        // Listener for show/hide menu items depending on if a text box is focused or not
        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(
            submitItem,
            deleteAccountItem,
            logoutItem)

        // Change toolbar button visibility if focused
        binding.username.onFocusChangeListener = editTextFocusListener
        binding.email.onFocusChangeListener = editTextFocusListener

        return super.onCreateOptionsMenu(menu)
    }

    // Handle actions for each toolbar menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.submit_profile -> {
                // Submit button - submits changes to user to firebase auth
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
                // Logout user
                viewModel.logout()
            }
            android.R.id.home -> {
                // Close activity if back pressed
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Display delete account confirmation dialog
    private fun showDeleteUserDialog() {
        // Setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Would you like to delete your account?")

        // Listener for positive and negative action buttons.
        // Yes button deletes account from firebase and shows message on success/failure.
        // No button closes dialog.
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