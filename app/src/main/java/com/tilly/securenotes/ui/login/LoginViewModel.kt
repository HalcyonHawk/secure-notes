package com.tilly.securenotes.ui.login

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.tilly.securenotes.data.repository.LoginRepository

class LoginViewModel: ViewModel() {
    fun loginWithEmail(email: String, password: String): Task<AuthResult>{
        return LoginRepository.loginWithEmail(email, password)
    }

    fun isUserLoggedIn(): Boolean{
        return LoginRepository.checkIfLoggedIn()
    }
}