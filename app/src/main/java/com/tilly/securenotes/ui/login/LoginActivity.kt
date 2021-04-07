package com.tilly.securenotes.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tilly.securenotes.R
import com.tilly.securenotes.data.repository.AuthRepository
import com.tilly.securenotes.databinding.ActivityLoginBinding
import com.tilly.securenotes.ui.notes.NotesActivity
import com.tilly.securenotes.ui.register.RegisterActivity
import com.tilly.securenotes.ui.reset_pass.ResetPassActivity

// Activity for login screen, allows user to go to registration page or login using existing account or google account
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // Request code for handling result of signing in using Google account
    private val RC_SIGN_IN = 4

    // Init firebase auth in activity for google login
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: LoginViewModel by viewModels()

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Start login and show loadingbar when login button is pressed
        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            // Login user with provided credentials and handle result of login
            viewModel.loginWithEmail(
                binding.username.text.toString(),
                binding.password.text.toString()
            ).addOnCompleteListener { task ->
                binding.loading.visibility = View.GONE

                // If login was successful then start notes activity otherwise show error message
                if (task.isSuccessful) {
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Login failed, try again", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        // Setting button size and click listener for sign in with Google account button
        binding.googleLogin.setSize(SignInButton.SIZE_STANDARD)
        binding.googleLogin.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // Starting account registration activity when create account button is pressed
        binding.createAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Starting reset password activity when reset password button is pressed
        binding.resetPassLink.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }
    }

    // Handle result of signing in using Google account
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            // Log in user if successful, show error message if not
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener {
                    googleSignIn(it.idToken!!)
                }
                .addOnFailureListener {
                    Log.w("Firebase", "Google sign in failed", it)
                    Toast.makeText(
                        baseContext,
                        "Authentication Failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }
    }

    // Sign in user to firebase authentication using token from google account
    private fun googleSignIn(token: String) {
        val viewModel: LoginViewModel by viewModels()
        viewModel.firebaseAuthWithGoogle(token)
            // Handle result of signing in, if successful start notes activity, if not show error message
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication Failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    override fun onStart() {
        super.onStart()
        val viewModel: ResetPassViewModel by viewModels()

        // Check if user already logged in when activity is started
        // Prevents logged in users having to log in again
        if (viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }
}