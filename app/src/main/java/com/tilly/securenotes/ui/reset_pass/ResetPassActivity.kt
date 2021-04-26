package com.tilly.securenotes.ui.reset_pass

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.tilly.securenotes.databinding.ActivityResetPassBinding
import com.tilly.securenotes.ui.login.ResetPassViewModel

// Reset Password Screen - Allow user to reset their password
class ResetPassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPassBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        // Showing back button in action bar
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val viewModel: ResetPassViewModel by viewModels()

        // Send request password email and show loadingbar when request loading
        binding.submit.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            viewModel.requestPassReset(binding.userEmail.text.toString())
                .addOnCompleteListener { task ->
                    binding.loading.visibility = View.GONE
                    // Display message on password reset success or failure
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset link sent. Please check your email.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this, "User not found, please try again.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
    }

    // Override options button functions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // Pressing the back button finishes the activity
            android.R.id.home -> {
                finish()
                true
            }
            else -> false
        }
    }
}