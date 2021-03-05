package com.tilly.securenotes.data.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage

object ProfileRepository {
    private val firestore = Firebase.firestore
    private val storageRef = Firebase.storage.reference


    // Users collection
    private fun getUsersCollection(): CollectionReference {
        return firestore.collection("users")
    }

    // Get current user ID
    private fun getUserId(): String{
        return AuthRepository.getFirebaseAuth().uid!!
    }

    // Delete user and return Task object to handle result
    fun deleteUser(): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.delete()
    }

    // Edit user's email on firebase authentication and return task to handle result
    fun editEmail(newEmail: String): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.updateEmail(newEmail)
    }

    fun editUsername(newName: String): Task<Void>{
        val updateProfile = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        return AuthRepository.getFirebaseUser()!!.updateProfile(updateProfile)
    }

    // TODO: Change to send verification email
    fun resetPassword(newPass: String): Task<Void>{
        return AuthRepository.getFirebaseUser()!!.updatePassword(newPass)
    }

    // Update profile pic url on firebase auth account
    fun updateProfilePicUrl(uri: Uri): Task<Void> {
        val updateProfile = UserProfileChangeRequest.Builder()
            .setPhotoUri(uri).build()
        return AuthRepository.getFirebaseUser()!!.updateProfile(updateProfile)
    }

    fun uploadProfilePicture(uri: Uri): UploadTask{
        val profileImageRef = storageRef.child(getUserId())
        return profileImageRef.putFile(uri)
    }

    fun getProfilePictureURL(): Task<Uri> {
        val profileImageRef = storageRef.child(getUserId())
        return profileImageRef.downloadUrl
    }
}