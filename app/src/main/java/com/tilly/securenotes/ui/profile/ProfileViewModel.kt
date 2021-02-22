package com.tilly.securenotes.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.data.model.User
import com.tilly.securenotes.data.repository.NoteRepository
import com.tilly.securenotes.data.repository.ProfileRepository
import com.tilly.securenotes.ui.notes.NotesUtility.observeOnce

class ProfileViewModel : ViewModel() {
    lateinit var initialUser: User

    private var _userName = MutableLiveData<String>("")
    private var _userEmail = MutableLiveData<String>("")
    private var _userAvatar = MutableLiveData<String>("")

    val userName: LiveData<String> get() = _userName
    val userEmail: LiveData<String> get() = _userEmail
    val userAvatar: LiveData<String> get() = _userAvatar

    private fun setUserName(name: String) {
        _userName.postValue(name)
    }

    private fun setEmail(email: String) {
        _userEmail.postValue(email)
    }

    private fun setAvatar(avatar: String) {
        _userAvatar.postValue(avatar)
    }

    fun logout() {
        ProfileRepository
    }

    fun commitChangesToDb(): LiveData<Boolean> {
        return ProfileRepository.editUser(
            name = userName.value,
            email = userEmail.value,
            avatar = userAvatar.value
        )
    }

    fun postUpdatedUser(user: User) {
        setUserName(user.name)
        setEmail(user.email)
        setAvatar(user.avatar)
    }

    // Get current user from firestore and alert the profile activity
    fun initCurrentUser(): LiveData<Boolean> {
        val userFindStatus = MutableLiveData<Boolean>()
        NoteRepository.getCurrentUser().observeOnce(Observer { result ->
            // Check if get user result is successful
            if (result is ResultStatusWrapper.Success) {
                // If successful then set initial user obj and post if user is found to observer in activity for handling
                initialUser = User(null, result.data.email, result.data.name, result.data.avatar)
                userFindStatus.postValue(true)
            } else if (result is ResultStatusWrapper.Error) {
                // Handle error response
                if (result.exception is NoSuchElementException) {
                    // If user not found then log and post result for handling in activity
                    Log.e("firebase", "User not found", result.exception)
                    userFindStatus.postValue(false)
                } else {
                    // If any other exception found, post result to activity and log
                    Log.e("firebase", result.message, result.exception)
                    userFindStatus.postValue(false)
                }
            }
        })
        return userFindStatus
    }
}