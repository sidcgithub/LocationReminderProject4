package com.udacity.project4.authentication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.udacity.project4.utils.FirebaseUserLiveData

enum class Authentication() {
    AUTHENTICATED,
    UNAUTHENTICATED
}

class AuthenticationViewModel : ViewModel() {

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            Authentication.AUTHENTICATED
        } else {
            Authentication.UNAUTHENTICATED
        }
    }

    fun isUserAuthenticated(): Authentication =
        if (FirebaseAuth.getInstance().currentUser != null) {
            Authentication.AUTHENTICATED
        } else {
            Authentication.UNAUTHENTICATED
        }
}