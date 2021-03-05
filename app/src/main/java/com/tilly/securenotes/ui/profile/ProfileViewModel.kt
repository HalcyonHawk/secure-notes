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

class ProfileViewModel : ViewModel() {
    private val user: FirebaseUser = AuthRepository.getFirebaseUser()!!


    val userName: String get() = user.displayName!!
    val userEmail: String get() = user.email!!
    val userAvatar: Uri? get() = user.photoUrl



    // Strings for edited username and email in text fields
    private val _profilePicUri = MutableLiveData<Uri?>()
    val profilePicUri: LiveData<Uri?> get() = _profilePicUri

    fun logout() {
        AuthRepository.signOut()
    }

    // Commit changes and return if successful as boolean or null if no changes
    fun commitChangedUser(editedUsername: String, editedEmail: String): LiveData<Boolean?> {
        val successLiveData: MutableLiveData<Boolean> = MutableLiveData()

        if (userName != editedUsername || user.email != editedEmail){
            // NAME AND EMAIL NEED UPDATE
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

            // EMAIL NEEDS UPDATE
            else if (user.email != editedEmail) {
                ProfileRepository.editEmail(editedEmail)
                    .addOnSuccessListener { successLiveData.postValue(true) }
                    .addOnFailureListener { successLiveData.postValue(false) }
            }
            // NAME NEEDS UPDATE
            else if (userName != editedUsername) {
                ProfileRepository.editUsername(editedUsername)
                    .addOnSuccessListener { successLiveData.postValue(true) }
                    .addOnFailureListener {
                        Log.e("firebase", "commitChangedUser: failed to update name", it )
                        successLiveData.postValue(false) }
            }
        }
        // NO CHANGE
        else {
            successLiveData.postValue(null)
        }

        return successLiveData
    }

    fun changePassword(pass: String): LiveData<Boolean>{
        val passChanged: MutableLiveData<Boolean> = MutableLiveData()
        user.updatePassword(pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    passChanged.postValue(true)
                } else {
                    Log.e("firebase", "Pass changed failed", task.exception)
                    passChanged.postValue(false)
                }

            }
        return passChanged
    }

    // Get current user from firestore and alert the profile activity
    fun initCurrentUser() {

//        val userFindStatus = MutableLiveData<Boolean>()
//        NoteRepository.getCurrentUser().observeOnce(Observer { result ->
//            // Check if get user result is successful
//            if (result is ResultStatusWrapper.Success) {
//                // If successful then set initial user obj and post if user is found to observer in activity for handling
//                // If no profile url on firebase user then set to default img
//                val userImg = AuthRepository.getFirebaseUser()!!.photoUrl?.toString() ?: DEFAULT_IMG
//
//                // TODO: Change avatar in user class to Uri type
//                user = User(
//                    dbUserId = AuthRepository.getFirebaseAuth().uid,
//                    email = result.data.email,
//                    name = result.data.name,
//                    avatar = userImg
//                )
//                userFindStatus.postValue(true)
//            } else if (result is ResultStatusWrapper.Error) {
//                // Handle error response
//                if (result.exception is NoSuchElementException) {
//                    // If user not found then log and post result for handling in activity
//                    Log.e("firebase", "User not found", result.exception)
//                    userFindStatus.postValue(false)
//                } else {
//                    // If any other exception found, post result to activity and log
//                    Log.e("firebase", result.message, result.exception)
//                    userFindStatus.postValue(false)
//                }
//            }
//        })
//        return userFindStatus
    }

    // Upload pic and return uri on success or null on error
    fun updateProfilePicture(uri: Uri){
        ProfileRepository.uploadProfilePicture(uri)
            .addOnFailureListener {
                Log.e("firebase", "updateProfilePicture: Failure", it)
            }
            .addOnCompleteListener{task ->
                if (task.isSuccessful){

                    loadNewProfilePicture()
                } else {
                    Log.e("firebase", "updateProfilePicture: ", task.exception)
                }
            }
    }

    fun deleteUser(): Task<Void> {

        return ProfileRepository.deleteUser()
            .addOnFailureListener {
                Log.e("firebase", "delete user: failed to delete user", it)
            }
    }

    fun loadProfilePicture(){
        ProfileRepository.getProfilePictureURL()
            .addOnSuccessListener { uri ->
                _profilePicUri.postValue(uri)

        }
            .addOnFailureListener {
                _profilePicUri.postValue(null)
                Log.e("firebase", "loadProfilePicture: failed to load profile pic", it)
            }
    }


    private fun loadNewProfilePicture(){
        ProfileRepository.getProfilePictureURL().addOnSuccessListener {uri ->
            ProfileRepository.updateProfilePicUrl(uri)
                .addOnCompleteListener {task ->
                    if (task.isSuccessful){
                        _profilePicUri.postValue(uri)
                    } else {
                        Log.e("firebase", "loadNewProfilePicture: cant get update url", task.exception)
                        _profilePicUri.postValue(null)
                    }
                }
        }.addOnFailureListener {
            Log.e("firebase", "loadNewProfilePicture: cant get dowload uri", it)
            _profilePicUri.postValue(null)
        }
    }

    fun registerAuthListener(listener: FirebaseAuth.AuthStateListener){
        AuthRepository.getFirebaseAuth().addAuthStateListener(listener)
    }
}