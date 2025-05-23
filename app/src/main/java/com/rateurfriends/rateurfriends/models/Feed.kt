package com.rateurfriends.rateurfriends.models

import com.google.firebase.firestore.Exclude

class Feed(

        val feedId: String = "",

        val timeStamp: Long = System.currentTimeMillis() / 1000L,

        val userId: String = "",

        var userName: String = "",

        val totalStarNumber: Int = 0,

        val meanStarNumber: Double = 0.0,

        val feedType: String = "",

        var country: String = "",

        var categoryName: String = "",

        val rating: Int = 0

) {

    @Exclude
    fun toLower(): Feed {
        this.userName = this.userName.toLowerCase()
        this.country = this.country.toLowerCase()
        this.categoryName = this.categoryName.toLowerCase()
        return this
    }

    @Exclude
    fun capitalize(): Feed {
        this.userName = this.userName.capitalize()
        this.categoryName = this.categoryName.capitalize()
        return this
    }

    @Exclude
    private fun getNearestThousand(number: Int): Int {
        return (number) / 1000 * 1000
    }

    var level: String = getLevelName()
        get() = getLevelName()

    @Exclude
    private fun getLevelName(): String {
        var level = ""
        if (levelNumber() < User.levelArray.size && levelNumber() >= 0) {
            level = User.levelArray[levelNumber()]
        }
        return level
    }

    @Exclude
    fun levelNumber(): Int {
        return this.totalStarNumber /1000
    }

    @Exclude
    fun getNearestStarThreshold(): Int {
        return getNearestThousand(this.totalStarNumber)
    }
}