package com.tilly.securenotes.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import com.tilly.securenotes.data.model.ResultStatusWrapper
import com.tilly.securenotes.data.model.User

object ProfileRepository {
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage

    // Users collection
    private fun getUsersCollection(): CollectionReference {
        return firestore.collection("users")
    }

    // Get current user ID
    private fun getUserId(): String{
        return AuthRepository.getFirebaseAuth().uid!!
    }



    fun editUser(name: String?,
                 email: String?,
                 avatar: String?): LiveData<Boolean>{
        val editSuccessful = MutableLiveData<Boolean>()
        val userMap = hashMapOf<String, String>()
        if (name != null) userMap["name"] = name
        if (email != null) userMap["email"] = email
        if (avatar != null) userMap["avatar"] = avatar

        getUsersCollection()
            .document(getUserId())
            .set(userMap)
            .addOnSuccessListener { documentSnapshot ->
                editSuccessful.postValue(true)
            }
            .addOnFailureListener { exception ->
                Log.e("firebase", "Edit user failed", exception)
                editSuccessful.postValue(false)
            }

        return editSuccessful
    }


    // Get current user by firebase auth id
    fun getCurrentUser(): LiveData<ResultStatusWrapper<User>> {
        val userLiveData: MutableLiveData<ResultStatusWrapper<User>> = MutableLiveData()
        getUsersCollection()
            .whereEqualTo("external_id", getUserId())
            .get()
            .addOnSuccessListener { queryResult ->
                val documents = queryResult.documents
                // Check if user document with ID from current firebase auth user is found
                if (documents.isNotEmpty()){
                    val doc = documents.first()
                    val user = User(dbDocId = doc.id,
                        email = doc.getString("email")!!,
                        name = doc.getString("external_id")!!,
                        avatar = doc.getString("avatar")!!)
                    userLiveData.postValue(ResultStatusWrapper.Success(user))
                } else {
                    // If user not found, return NoSuchElementException in result
                    userLiveData.postValue(
                        ResultStatusWrapper.Error(null,
                        exception = NoSuchElementException("User not found")))
                }
            }
            .addOnFailureListener { exception ->
                userLiveData.postValue(ResultStatusWrapper.Error(null, exception))
            }

        return userLiveData
    }
}