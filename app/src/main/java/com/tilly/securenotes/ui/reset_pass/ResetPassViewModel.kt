package com.tilly.securenotes.ui.login

import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.tilly.securenotes.data.repository.AuthRepository

class ResetPassViewModel: ViewModel() {
    fun requestPassReset(email: String): Task<Void>{
        return AuthRepository.resetPass(email)
    }

    fun isUserLoggedIn(): Boolean{
        return AuthRepository.checkIfLoggedIn()
    }

}