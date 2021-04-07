package com.tilly.securenotes.ui.login

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.tilly.securenotes.data.repository.AuthRepository

// ViewModel allowing user to sign in using app account or Google account through the AuthRepository object
class LoginViewModel: ViewModel() {

    // Sign in user using an email and password
    fun loginWithEmail(email: String, password: String): Task<AuthResult>{
        return AuthRepository.loginWithEmail(email, password)
    }

    // Sign in user using Google account
    fun firebaseAuthWithGoogle(idToken: String): Task<AuthResult> {
        // Get sign in token for user's google account
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return AuthRepository.getFirebaseAuth().signInWithCredential(credential)
    }
}