package com.tilly.securenotes.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tilly.securenotes.data.repository.AuthRepository
import com.tilly.securenotes.data.repository.ProfileRepository

// ViewModel for profile activity
class ProfileViewModel : ViewModel() {
    private val user: FirebaseUser = AuthRepository.getFirebaseUser()!!

    val userName: String get() = user.displayName!!
    val userEmail: String get() = user.email!!

    // Strings for edited username and email in text fields
    private val _profilePicUri = MutableLiveData<Uri?>()
    val profilePicUri: LiveData<Uri?> get() = _profilePicUri

    // Log user out from the app
    fun logout() {
        AuthRepository.signOut()
    }

    // Submit changes and return if successful as boolean or null if no changes
    fun commitChangedUser(editedUsername: String, editedEmail: String): LiveData<Boolean?> {
        // Return temporary LiveData object to handle success/failure of editing user in firebase auth
        val successLiveData: MutableLiveData<Boolean> = MutableLiveData()

        // Check which fields need updating in firebase auth based on. If the new values match
        // the old values, then make changes to firebase auth and return boolean of result depending
        // on if the change was successful or not
        if (userName != editedUsername || user.email != editedEmail) {
            // UPDATE NAME AND EMAIL
            if (userName != editedUsername && userEmail != editedEmail) {
                // Edit username, if successful then edit email and post success response
                ProfileRepository.editUsername(editedUsername)
                    .addOnSuccessListener {
                        ProfileRepository.editEmail(editedEmail)
                            .addOnSuccessListener { successLiveData.postValue(true) }
                            .addOnFailureListener { successLiveData.postValue(false) }
                    }
                    .addOnFailureListener { successLiveData.postValue(false) }
            }

            // UPDATE EMAIL
            else if (user.email != editedEmail) {
                ProfileRepository.editEmail(editedEmail)
                    .addOnSuccessListener { successLiveData.postValue(true) }
                    .addOnFailureListener { successLiveData.postValue(false) }
            }
            // UPDATE NAME
            else if (userName != editedUsername) {
                ProfileRepository.editUsername(editedUsername)
                    .addOnSuccessListener { successLiveData.postValue(true) }
                    .addOnFailureListener {
                        Log.e("firebase", "commitChangedUser: failed to update name", it)
                        successLiveData.postValue(false)
                    }
            }
        }
        // NO CHANGE
        else {
            successLiveData.postValue(null)
        }

        return successLiveData
    }

    // Change password and return task to view for result handling
    fun changePassword(pass: String): Task<Void> {
        return user.updatePassword(pass)
    }

    // Upload profile picture and load new picture if successful, log exception on failure
    fun updateProfilePicture(uri: Uri) {
        ProfileRepository.uploadProfilePicture(uri)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateNewPicInAuth()
                } else {
                    Log.e("firebase", "Failed to update profile picture", task.exception)
                }
            }
    }

    // Delete user from firebase authentication and return task for handling in view
    fun deleteUser(): Task<Void> {
        return ProfileRepository.deleteUser()
            .addOnFailureListener {
                // Log exception if failed to delete user from firebase
                Log.e("firebase", "Failed to delete user", it)
            }
    }

    // Load new profile picture from URL in firebase auth into LiveData to update in view
    fun loadProfilePicture() {
        ProfileRepository.getProfilePictureURL()
            .addOnSuccessListener { uri ->
                _profilePicUri.postValue(uri)
            }
            .addOnFailureListener {
                _profilePicUri.postValue(null)
            }
    }

    // Load a newly set profile picture
    // Get profile picture URI from firebase cloud storage, then submit changed URI to firebase auth account
    private fun updateNewPicInAuth() {
        ProfileRepository.getProfilePictureURL().addOnSuccessListener { uri ->
            ProfileRepository.updateProfilePicUrl(uri)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _profilePicUri.postValue(uri)
                    } else {
                        Log.e(
                            "firebase",
                            "Cant get updated uri",
                            task.exception
                        )
                        _profilePicUri.postValue(null)
                    }
                }
        }.addOnFailureListener {
            Log.e("firebase", "Cant get download uri", it)
            _profilePicUri.postValue(null)
        }
    }

    // Register listener to detect and respond to when user has been logged out
    fun registerAuthListener(listener: FirebaseAuth.AuthStateListener) {
        AuthRepository.getFirebaseAuth().addAuthStateListener(listener)
    }
}