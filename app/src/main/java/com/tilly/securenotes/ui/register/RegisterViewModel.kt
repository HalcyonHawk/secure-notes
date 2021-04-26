package com.tilly.securenotes.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tilly.securenotes.data.repository.AuthRepository

// Viewmodel for register activity
class RegisterViewModel: ViewModel() {
    // Register new account on firebase
    fun registerAccount(email: String,
                        password: String, displayName: String): LiveData<Boolean>{
        return AuthRepository.createAccount(email, password, displayName)

    }
}