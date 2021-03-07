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


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    val RC_SIGN_IN = 4

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




        // Start login and show loadingbar
        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            viewModel.loginWithEmail(
                binding.username.text.toString(),
                binding.password.text.toString()
            )
                .addOnCompleteListener { task ->
                    binding.loading.visibility = View.GONE

                    if (task.isSuccessful) {
                        val intent = Intent(this, NotesActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Login failed, try again", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
        }

        binding.googleLogin.setSize(SignInButton.SIZE_STANDARD)
        binding.googleLogin.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        binding.createAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.resetPassLink.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener {
                    googleSignIn(it.idToken!!)
                }
                .addOnFailureListener {
                    Log.w("Firebase", "Google sign in failed", it)
                    Snackbar.make(
                        binding.root,
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

        }
    }

    fun googleSignIn(token: String){
        val viewModel: LoginViewModel by viewModels()
        viewModel.firebaseAuthWithGoogle(token)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Firebase", "signInWithCredential:success")
                    val intent = Intent(this, NotesActivity::class.java)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Firebase", "signInWithCredential:failure", task.exception)
                    Snackbar.make(
                        binding.root,
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }


    override fun onStart() {
        super.onStart()
        val viewModel: ResetPassViewModel by viewModels()

        // Check if user already logged in
        if (viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, NotesActivity::class.java))
        }
    }
}