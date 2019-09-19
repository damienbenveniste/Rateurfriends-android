package com.rateurfriends.rateurfriends.models

import com.google.firebase.firestore.Exclude

class Contact(

        var phoneName: String = "",

        val phoneNumber: String = "",

        var userId: String = "",

        val timeStamp: Long = System.currentTimeMillis() / 1000L,

        @get:Exclude
        var knownUser: Boolean = false,

        @get: Exclude
        var invited: Boolean = false

) {

        @Exclude
        fun toLower(): Contact {
                this.phoneName = this.phoneName.toLowerCase()
                return this
        }

        @Exclude
        fun capitalize(): Contact {
                this.phoneName = this.phoneName.capitalize()
                return this
        }
}