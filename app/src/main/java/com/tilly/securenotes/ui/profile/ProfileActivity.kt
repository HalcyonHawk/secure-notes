package com.tilly.securenotes.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kroegerama.imgpicker.BottomSheetImagePicker
import com.kroegerama.imgpicker.ButtonType
import com.squareup.picasso.Picasso
import com.tilly.securenotes.R
import com.tilly.securenotes.data.model.EditorToolbarActivity
import com.tilly.securenotes.data.model.User
import com.tilly.securenotes.databinding.ActivityProfileBinding
import com.tilly.securenotes.ui.login.LoginActivity

class ProfileActivity : EditorToolbarActivity(), BottomSheetImagePicker.OnImagesSelectedListener {
    lateinit var binding: ActivityProfileBinding
    lateinit var viewModel: ProfileViewModel
    val doge = "https://www.telegraph.co.uk/content/dam/technology/2021/01/28/Screenshot-2021-01-28-at-13-20-35_trans_NvBQzQNjv4BqgorLzNIuWKFjctv8STCiZsyL_iAq5T7dsR69ZpavGbo.png?imwidth=960"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflating activity layout using binding class to access views without findViewById()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        // Setting activity view to root of
        setContentView(binding.root)
        // Set custom toolbar view as action bar
        setSupportActionBar(binding.topAppBar)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)


        // Check if currently logged in firebase authentication user is found in
        viewModel.initCurrentUser().observe(this, Observer { isUserFound ->
            if (isUserFound) {
                initViews(viewModel.initialUser)
            } else {
                Toast.makeText(this, "Current user not found", Toast.LENGTH_SHORT).show()


            }
        })

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
    }

    private fun initViews(user: User){
        Picasso.get().load(doge).into(binding.profileImage)
        binding.username.setText(user.name)
        binding.email.setText(user.email)

    }

    fun logout(){
        val i = Intent(this, LoginActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TASK or
                Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if(resultCode == Activity.RESULT_OK){
            val imageUri = intent?.data
            Picasso.get().load(imageUri).into(binding.profileImage)
        }
    }

    // Inflate options menu and observe if name or email are being edited to show/hide toolbar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)


        val submitItem = menu?.findItem(R.id.submit_profile)
        val deleteAccountItem = menu?.findItem(R.id.delete_account)
        val logoutItem = menu?.findItem(R.id.logout)

        val editTextFocusListener = getUpdateToolbarIfEditingListener(submitItem, deleteAccountItem, logoutItem)

        // Changing toolbar button visibility if focused
        binding.username.onFocusChangeListener = editTextFocusListener
        binding.email.onFocusChangeListener = editTextFocusListener


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.submit_profile -> {}
            R.id.delete_account -> {}
            R.id.logout -> { viewModel.logout()}

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onImagesSelected(uris: List<Uri>, tag: String?) {
        uris.forEach { uri ->
            Picasso.get().load(uri).into(binding.profileImage)
        }
    }
}