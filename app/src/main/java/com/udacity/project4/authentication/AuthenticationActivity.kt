package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding: ActivityAuthenticationBinding
    val vm by viewModels<AuthenticationViewModel>()
    lateinit var activityIntent: Intent


    companion object {
        private const val TAG = "AuthenticationActivity"
        const val SIGN_IN_REQUEST_CODE = 1234
    }

//    var resultLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//
//            val data: Intent? = result.data
//            val response = IdpResponse.fromResultIntent(data)
//            when(vm.isUserAuthenticated()) {
//                Authentication.AUTHENTICATED ->
//                {
//                    if (result.resultCode == Activity.RESULT_OK) {
//                        Log.i(
//                            TAG,
//                            "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
//
//                        )
//                        navToReminderActivity()
//                    } else {
//                        Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
//                    }
//                }
//                Authentication.UNAUTHENTICATED -> {}
//            }
//
//
//
//        }

    private fun navToReminderActivity() {
        startActivity(activityIntent)
        this@AuthenticationActivity.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityIntent = Intent(this@AuthenticationActivity, RemindersActivity::class.java)
        activityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication);
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.loginPageButton.setOnClickListener {
            launchSignInFlow()
        }
//          TODO: If the user was authenticated, send him to RemindersActivity
//       when(vm.isUserAuthenticated()) {
//            Authentication.AUTHENTICATED -> navToReminderActivity()
//            Authentication.UNAUTHENTICATED -> {}
//        }
        observeAuthenticationState()



//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun observeAuthenticationState() {

        vm.authenticationState.observe(this, Observer { authenticationState ->

            when (authenticationState) {

                Authentication.AUTHENTICATED -> {
                    navToReminderActivity()
                }
                else -> {

                    binding.loginPageButton.setOnClickListener {
                        launchSignInFlow()
                    }
                }
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_REQUEST_CODE
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

}
