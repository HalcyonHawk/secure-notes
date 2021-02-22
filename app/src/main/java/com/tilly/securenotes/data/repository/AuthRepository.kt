package com.tilly.securenotes.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object AuthRepository {
    private var auth: FirebaseAuth = Firebase.auth
    private var firestore = Firebase.firestore

    fun getFirebaseAuth(): FirebaseAuth{
        return auth
    }

    fun loginWithEmail(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun createAccount(email: String,
                      password: String,
                      displayName: String): LiveData<Boolean> {
        val resultLiveData: MutableLiveData<Boolean> = MutableLiveData()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ result ->
            if (result.isSuccessful){
                // If user crated, add to db
                val user = hashMapOf(
                    "avatar" to "",
                    "email" to email,
                    "external_id" to auth.currentUser!!.uid,
                    "name" to displayName)
                firestore.collection("users")
                    .add(user)
                    .addOnSuccessListener {
                        resultLiveData.postValue(true)
                    }
                    .addOnFailureListener{
                        resultLiveData.postValue(false)
                        throw it
                    }

            } else {
                // If user not created, post failed result to subscribers
                resultLiveData.postValue(false)
            }
        }

        return resultLiveData
    }

    fun checkIfLoggedIn(): Boolean{
        return auth.currentUser != null
    }

}
