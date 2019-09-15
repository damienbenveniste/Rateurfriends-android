package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.BuildConfig
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.helperClasses.Globals

class LoginController(
        val activity: Activity
) {

    private val SIGN_IN = 123
    private val providers = arrayListOf(
            AuthUI.IdpConfig.PhoneBuilder().build()
    )

    fun handleUserLoginIntent(firstTimeIntent: Intent, nextTimeIntent: Intent) {

        if (FirebaseAuth.getInstance().currentUser != null) {
            login(firstTimeIntent, nextTimeIntent)
        } else {
            checkAuthentication()
        }
    }

    fun handleOnActivityResult(requestCode: Int,
                               resultCode: Int,
                               data: Intent?,
                               firstTimeIntent: Intent,
                               nextTimeIntent: Intent) {

        if(requestCode == SIGN_IN && resultCode == Activity.RESULT_OK){
            login(firstTimeIntent, nextTimeIntent)
        } else {
            val response = IdpResponse.fromResultIntent(data)
            when {
                response == null -> {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                    ).show()
                }
                response.error?.errorCode == ErrorCodes.NO_NETWORK -> {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.network_error),
                            Toast.LENGTH_SHORT
                    ).show()
                }
                response.error?.errorCode == ErrorCodes.UNKNOWN_ERROR -> {
                    Toast.makeText(
                            activity,
                            activity.getString(R.string.unknown_error),
                            Toast.LENGTH_SHORT
                    ).show()
                }
                else ->  Toast.makeText(
                        activity,
                        activity.getString(R.string.unknown_error),
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun checkAuthentication() {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                        .setAvailableProviders(providers)
                        .setTosAndPrivacyPolicyUrls(
                                "link to app terms and service",
                                "link to app privacy policy")
                            .setTheme(R.style.LoginTheme)
                        .build(),
                SIGN_IN)
    }


    private fun login(firstTimeIntent: Intent, nextTimeIntent: Intent) {

        if (isFirstTimeLogin()) {
            activity.startActivity(firstTimeIntent)
            val prefs = activity.getSharedPreferences(
                    activity.getString(R.string.shared_preference_file),
                    Context.MODE_PRIVATE
            )
            prefs.edit().putBoolean(activity.getString(R.string.first_time_login), false).apply()
        } else {
            Globals.getInstance().setUser()
            activity.startActivity(nextTimeIntent)
        }
    }

    private fun isFirstTimeLogin(): Boolean {

        val prefs = activity.getSharedPreferences(
                activity.getString(R.string.shared_preference_file),
                Context.MODE_PRIVATE
        )

        val firstTimeLogin = prefs.getBoolean(
                activity.getString(R.string.first_time_login),
                true
        )

        val informationCollected = prefs.getBoolean(
                activity.getString(R.string.information_collected),
                false
        )

        return firstTimeLogin || !informationCollected
    }



}