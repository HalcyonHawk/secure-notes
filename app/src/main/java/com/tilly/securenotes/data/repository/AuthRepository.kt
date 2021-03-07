package com.tilly.securenotes.data.repository


import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

// AuthRepository object providing access to all authentication related data sources i.e Firebase Authentication
object AuthRepository {
    private var auth: FirebaseAuth = Firebase.auth



    // Getter for to access FirebaseAuth object
    fun getFirebaseAuth(): FirebaseAuth{
        return auth
    }

    // Get currently logged in user object
    fun getFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Login with email and password using firebase authentication
    fun loginWithEmail(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    // Create account using email, password and displayed name parameters and return result as LiveData
    fun createAccount(email: String,
                      password: String,
                      displayName: String): LiveData<Boolean> {
        // Creating temporary boolean LiveData to return result of creating account to view for handling
        val resultLiveData: MutableLiveData<Boolean> = MutableLiveData()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ result ->
            if (result.isSuccessful){
                // If creating user on firebase is successful then set their display name on firebase
                val displayNameUpdateReq = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()

                // Submit update display name request to firebase
                result.result!!.user!!.updateProfile(displayNameUpdateReq)
                    .addOnCompleteListener { nameUpdateResult ->
                        // Post result of setting user's display name to observers in view
                        resultLiveData.postValue(nameUpdateResult.isSuccessful)
                    }
            } else {
                // Logging exception if user not successfully created
                Log.e("firebase", "createAccount: failed", result.exception)
                // If user not created, post failed result to subscribers
                resultLiveData.postValue(false)
            }
        }
        // Return as non-mutable LiveData for subscribing only
        return resultLiveData
    }

    // Function to sign out from firebase
    fun signOut(){
        auth.signOut()
    }

    // Function to check if the user is logged in without exposing private firebase authentication object
    fun checkIfLoggedIn(): Boolean{
        return auth.currentUser != null
    }

    fun resetPass(email: String): Task<Void> {
        return auth.sendPasswordResetEmail(email)

    }
}
