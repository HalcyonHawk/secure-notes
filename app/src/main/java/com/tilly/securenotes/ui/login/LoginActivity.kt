package com.tilly.securenotes.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
import com.tilly.securenotes.ui.notes.NotesActivity
import com.tilly.securenotes.databinding.ActivityLoginBinding
import com.tilly.securenotes.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: LoginViewModel by viewModels()


        binding.login.setOnClickListener {
            viewModel.loginWithEmail(binding.username.text.toString(), binding.password.text.toString())
                .addOnCompleteListener{ task ->
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
    }

    override fun onStart() {
        super.onStart()
        val viewModel: LoginViewModel by viewModels()

        // Check if user already logged in
        if(viewModel.isUserLoggedIn()){
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }
}