package com.tilly.securenotes.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

// Repository object giving access to profile related data sources, i.e. cloud storage and firestore
object ProfileRepository {
    private val firestore = Firebase.firestore
    private val storageRef = Firebase.storage.reference

    // Get current user ID of firebase auth user
    private fun getUserId(): String{
        return AuthRepository.getFirebaseAuth().uid!!
    }

    // Delete user and return Task to handle result in ViewModel or view
    fun deleteUser(): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.delete()
    }

    // Edit user's email on firebase authentication and return task to handle result
    fun editEmail(newEmail: String): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.updateEmail(newEmail)
    }

    // Edit current user's username in firebase auth and return task for handling in view
    fun editUsername(newName: String): Task<Void>{
        val updateProfile = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        return AuthRepository.getFirebaseUser()!!.updateProfile(updateProfile)
    }

    // Update profile pic url on firebase auth account
    fun updateProfilePicUrl(uri: Uri): Task<Void> {
        val updateProfile = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri).build()
        return AuthRepository.getFirebaseUser()!!.updateProfile(updateProfile)
    }

    // Upload a profile picture from given URI to firebase cloud storage then return UploadTask object for handling
    fun uploadProfilePicture(uri: Uri): UploadTask{
        val profileImageRef = storageRef.child(getUserId())
        return profileImageRef.putFile(uri)
    }

    // Get URI to currently set profile picture
    fun getProfilePictureURL(): Task<Uri> {
        val profileImageRef = storageRef.child(getUserId())
        return profileImageRef.downloadUrl
    }
}