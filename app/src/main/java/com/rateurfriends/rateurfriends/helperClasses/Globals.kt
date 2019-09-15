package com.rateurfriends.rateurfriends.helperClasses

import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.models.User

class Globals// Restrict the constructor from being instantiated
private constructor() {

    // Global variable
    var user: User? = null

    fun setUser() {
        if (user == null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                UserDAO.getUser(currentUser.uid) { this.user = it }
            }
        }
    }

    fun setUser(callback: (User) -> Unit) {
        if (user == null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                UserDAO.getUser(currentUser.uid) {
                    this.user = it
                    callback(it)
                }
            }
        }
    }

    companion object {
        private var instance: Globals? = null

        @Synchronized
        fun getInstance(): Globals {
            if (instance == null) {
                instance = Globals()
            }
            return instance!!
        }
    }
}