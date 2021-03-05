package com.tilly.securenotes.ui.profile

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.tilly.securenotes.ui.login.LoginActivity
import com.tilly.securenotes.utilities.InputFocusUtilities
import com.tilly.securenotes.utilities.NotesUtility.observeOnce

class ProfileActivity : AppCompatActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    lateinit var binding: ActivityProfileBinding
    lateinit var viewModel: ProfileViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        //TODO: Dark mode
        val darkModeEnabled = sharedPrefs.getBoolean("darkMode", false)
        if (darkModeEnabled){
            setTheme(R.style.Theme_MaterialComponents_DayNight_NoActionBar)
        }

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
            if (uri != null) {
                Picasso.get().load(uri).into(binding.profileImage)
            } else {
                binding.profileImage.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_account_box_black_24dp, null))

            }
        })

        // Register listener for auth state change
        viewModel.registerAuthListener(FirebaseAuth.AuthStateListener { auth ->
            if (auth.currentUser == null) {
                goBackToLogin()
            }
        })

        binding.submitPass.setOnClickListener {
            if (binding.newPassword.text.toString() == binding.confirmNewPassword.text.toString()) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                viewModel.changePassword(binding.newPassword.text.toString())
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
        // Initialise text fields
        binding.username.setText(viewModel.userName)
        binding.email.setText(viewModel.userEmail)

        // Load profile pic
        viewModel.loadProfilePicture()

        binding.toggleTheme.isChecked = darkModeEnabled

        binding.toggleTheme.setOnCheckedChangeListener { buttonView, isChecked ->
            with(sharedPrefs.edit()){
                putBoolean("darkMode", isChecked)
                apply()
            }
            applicationContext.setTheme(R.style.Theme_MaterialComponents_DayNight_NoActionBar)
        }
    }

    fun goBackToLogin(){
        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
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

        val editTextFocusListener = InputFocusUtilities.getUpdateMenuIfEditingListener(
            submitItem,
            deleteAccountItem,
            logoutItem
        )

        // Changing toolbar button visibility if focused
        binding.username.onFocusChangeListener = editTextFocusListener
        binding.email.onFocusChangeListener = editTextFocusListener

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.submit_profile -> {
                viewModel.commitChangedUser(
                    binding.username.text.toString(),
                    binding.email.text.toString()
                ).observeOnce(Observer {
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
                showDeleteUserDialog()
            }
            R.id.logout -> {
                viewModel.logout()
            }
            android.R.id.home -> {
                // Close activity if back pressed
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showDeleteUserDialog() {
        // setup the alert builder
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account")
        builder.setMessage("Would you like to delete your account?")

        // add the buttons
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

        // create and show the alert dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}