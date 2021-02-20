package com.tilly.securenotes.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.tilly.securenotes.data.repository.LoginRepository

class RegisterViewModel: ViewModel() {
    //TODO: Change to give better response than boolean
    fun registerAccount(email: String,
                        password: String, displayName: String): LiveData<Boolean>{
        return LoginRepository.createAccount(email, password, displayName)
    }
}