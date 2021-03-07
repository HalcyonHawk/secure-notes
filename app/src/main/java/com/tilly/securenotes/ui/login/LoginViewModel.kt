package com.tilly.securenotes.ui.login

import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.tilly.securenotes.data.repository.AuthRepository
import com.tilly.securenotes.ui.notes.NotesActivity

class LoginViewModel: ViewModel() {

    fun loginWithEmail(email: String, password: String): Task<AuthResult>{
        return AuthRepository.loginWithEmail(email, password)
    }

    fun isUserLoggedIn(): Boolean{
        return AuthRepository.checkIfLoggedIn()
    }

    fun firebaseAuthWithGoogle(idToken: String): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return AuthRepository.getFirebaseAuth().signInWithCredential(credential)

    }

}