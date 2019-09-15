package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.helperClasses.Utils
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User

class InfoCollectionController(val activity: Activity) {

    fun submitInformation(editText: EditText, intentNext: Intent,
                          progressLayout: FrameLayout) {
        val nameText = editText.text.toString()
        if (nameText.isNotEmpty()) {

            progressLayout.visibility = View.VISIBLE

            updateDisplayName(nameText) {
                saveUser {
                    val prefs = activity.getSharedPreferences(
                            activity.getString(R.string.shared_preference_file),
                            Context.MODE_PRIVATE
                    )
                    prefs.edit().putBoolean(
                            activity.getString(R.string.information_collected),
                            true
                    ).apply()

                    intentNext.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    activity.startActivity(intentNext)
                }

            }
        }
    }

    private fun saveUser(callback: () -> Unit) {
        UserDAO.checkUserExistWithUserId(FirebaseAuth.getInstance().currentUser!!.uid) {
            if (it) {
                updateOldUser { callback() }
            } else {
                saveNewUser { callback() }
            }
        }
    }

    private fun updateOldUser(callback: () -> Unit) {

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = currentUser.uid
        val userName = currentUser.displayName
        val phoneNumber = currentUser.phoneNumber
        val country = Utils.getCountryBasedOnSimCardOrNetwork(activity)

        if (userName != null && phoneNumber != null) {
            val map = mapOf(
                    "userName" to userName,
                    "country" to country
            )
            UserDAO.updateUser(userId, map) {
                it.userName = userName
                it.country = country
                Globals.getInstance().user = it
                callback()
            }
        }

    }

    private fun saveNewUser(callback: () -> Unit) {

        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val userId = currentUser.uid
        val userName = currentUser.displayName
        val phoneNumber = currentUser.phoneNumber
        val country = Utils.getCountryBasedOnSimCardOrNetwork(activity)
        val categoryList = Category.initialCategories.map{Category(it, userId)}

        if (userName != null && phoneNumber != null) {
            val user = User(userName, phoneNumber, userId, country=country)
            UserDAO.insertUser(user, categoryList, activity) {
                Globals.getInstance().user = user
                callback()
            }
        }
    }

    private fun updateDisplayName(name: String, callback: () -> Unit) {

        val profileUpdates = UserProfileChangeRequest
                .Builder()
                .setDisplayName(name)
                .build()

        FirebaseAuth.getInstance()
                .currentUser!!
                .updateProfile(profileUpdates)
                .addOnCompleteListener {
                    task ->
                    if (task.isSuccessful) {
                        callback()
                    } else {
                        Toast.makeText(
                                activity,
                                activity.getString(R.string.information_not_saved),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }
}