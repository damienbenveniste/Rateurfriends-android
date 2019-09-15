package com.rateurfriends.rateurfriends.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.FirebaseDatabase
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Category(

        // TODO: add user name and user id
        val categoryName: String = "",

        val userId: String = "",

        var publicVisibility: Boolean = true,

        var starNumber: Int = 5,

        var voteNumber: Int = 1,

        val timeStamp: Long = System.currentTimeMillis() / 1000L

) {

    @Exclude
    fun changePublicVisibility(userId: String) {
        CategoryDAO.setPublicVisibility(this, userId, !this.publicVisibility) {
            this.publicVisibility = !this.publicVisibility
        }
    }

    var meanStarNumber: Float = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)
        get() = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)

    companion object {

        @get: Exclude
        val initialCategories = arrayOf(
                "Honest",
                "Loyal",
                "Smart",
                "Attractive",
                "Authentic",
                "Generous",
                "Kind"
        )
    }

}