package com.tilly.securenotes.ui.login

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.tilly.securenotes.data.repository.AuthRepository

class LoginViewModel: ViewModel() {
    fun loginWithEmail(email: String, password: String): Task<AuthResult>{
        return AuthRepository.loginWithEmail(email, password)
    }

    fun isUserLoggedIn(): Boolean{
        return AuthRepository.checkIfLoggedIn()
    }

}