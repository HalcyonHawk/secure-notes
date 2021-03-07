package com.tilly.securenotes.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.tilly.securenotes.ui.notes.NotesActivity
import com.tilly.securenotes.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewModel: RegisterViewModel by viewModels()

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.register.setOnClickListener {
            // If password confirmation correct then register user else show error
            if (binding.password.text.toString() == binding.confirmPassword.text.toString()){
                if (binding.password.text.length < 6) {
                    Toast.makeText(
                        this,
                        "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    binding.loading.visibility = View.VISIBLE
                    // Observe and handle registration result
                    viewModel.registerAccount(binding.username.text.toString(),
                        binding.password.text.toString(),
                        binding.displayName.text.toString())
                        .observe(this, Observer { success ->
                            binding.loading.visibility = View.VISIBLE

                            if (success) {

                                val intent = Intent(this, NotesActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            } else {
                Toast.makeText(this, "Passwords don't match, try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> false
        }
    }
}