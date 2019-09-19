package com.rateurfriends.rateurfriends.models

class Vote (
        val rating: Int = 0,

        val userId: String = "",

        val contactId: String = "",

        val categoryName: String = "",

        val timeStamp: Long = System.currentTimeMillis() / 1000L
) {

    fun getRatingStars(): String {

        var initString = ""

        for (i in 1..rating) {
            initString += "★"
        }

        for (i in (rating + 1)..5) {
            initString += "☆"
        }

        return initString
    }
}