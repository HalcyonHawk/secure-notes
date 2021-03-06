package com.tilly.securenotes.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.tilly.securenotes.R
import com.tilly.securenotes.ui.notes.NotesActivity
import com.tilly.securenotes.databinding.ActivityLoginBinding
import com.tilly.securenotes.ui.register.RegisterActivity
import com.tilly.securenotes.ui.reset_pass.ResetPassActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        val darkModeEnabled = sharedPrefs.getBoolean("darkMode", false)
        if (darkModeEnabled){
            applicationContext.setTheme(R.style.Theme_MaterialComponents_DayNight_NoActionBar)
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: LoginViewModel by viewModels()


        // Start login and show loadingbar
        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            viewModel.loginWithEmail(binding.username.text.toString(), binding.password.text.toString())
                .addOnCompleteListener{ task ->
                    binding.loading.visibility = View.GONE

                    if (task.isSuccessful){
                        val intent = Intent(this, NotesActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Login failed, try again", Toast.LENGTH_SHORT)
                            .show()
                    }

            }
        }

        binding.createAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.resetPassLink.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val viewModel: ResetPassViewModel by viewModels()

        // Check if user already logged in
        if(viewModel.isUserLoggedIn()){
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }
}