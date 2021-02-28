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

// TODO: Remove
    private fun updateUserDbDoc(editedFields: Map<String, String>): Task<Void>{
        return getUsersCollection()
            .document(getUserId())
            .update(editedFields)
    }

    fun deleteUser(): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.delete()
    }

    fun editEmail(newEmail: String): Task<Void> {
        return AuthRepository.getFirebaseUser()!!.updateEmail(newEmail)
    }

    fun editUsername(newName: String): Task<Void>{
//        return updateUserDbDoc(hashMapOf("name" to newName))
        val updateProfile = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()
        return AuthRepository.getFirebaseUser()!!.updateProfile(updateProfile)
    }

    // TODO: Change to send verification email
    fun resetPassword(newPass: String): Task<Void>{
        return AuthRepository.getFirebaseUser()!!.updatePassword(newPass)
    }

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


    // Get current user by firebase auth id
//    fun getCurrentUser(): LiveData<ResultStatusWrapper<User>> {
//        val userLiveData: MutableLiveData<ResultStatusWrapper<User>> = MutableLiveData()
//        getUsersCollection()
//            .whereEqualTo("external_id", getUserId())
//            .get()
//            .addOnSuccessListener { queryResult ->
//                val documents = queryResult.documents
//                // Check if user document with ID from current firebase auth user is found
//                if (documents.isNotEmpty()){
//                    val doc = documents.first()
//                    val user = User(dbUserId = doc.id,
//                        email = doc.getString("email")!!,
//                        name = doc.getString("external_id")!!,
//                        avatar = doc.getString("avatar")!!)
//                    userLiveData.postValue(ResultStatusWrapper.Success(user))
//                } else {
//                    // If user not found, return NoSuchElementException in result
//                    userLiveData.postValue(
//                        ResultStatusWrapper.Error(null,
//                        exception = NoSuchElementException("User not found")))
//                }
//            }
//            .addOnFailureListener { exception ->
//                userLiveData.postValue(ResultStatusWrapper.Error(null, exception))
//            }
//
//        return userLiveData
//    }
}