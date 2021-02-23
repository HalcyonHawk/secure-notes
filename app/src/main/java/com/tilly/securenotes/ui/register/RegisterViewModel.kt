package com.tilly.securenotes.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tilly.securenotes.data.repository.AuthRepository

class RegisterViewModel: ViewModel() {
    //TODO: Change to give better response than boolean
    fun registerAccount(email: String,
                        password: String, displayName: String): LiveData<Boolean>{
        return AuthRepository.createAccount(email, password, displayName)

    }
}