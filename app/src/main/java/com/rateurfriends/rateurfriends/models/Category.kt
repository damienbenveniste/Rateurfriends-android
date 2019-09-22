package com.rateurfriends.rateurfriends.models


import com.google.firebase.firestore.Exclude
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import kotlin.math.max
import kotlin.math.min

class Category(

        // TODO: add user name and user id
        var categoryName: String = "",

        val userId: String = "",

        var public: Boolean = true,

        var starNumber: Int = 5,

        var voteNumber: Int = 1,

        val timeStamp: Long = System.currentTimeMillis() / 1000L

) {

    @Exclude
    fun toLower(): Category {
        this.categoryName = this.categoryName.toLowerCase()
        return this
    }

    @Exclude
    fun capitalize(): Category {
        this.categoryName = this.categoryName.capitalize()
        return this
    }

    @Exclude
    fun changePublicVisibility(userId: String) {
        CategoryDAO.setPublicVisibility(this, userId, !this.public,
                onSuccess = {
                    this.public = !this.public
                },
                onFailure = {
                    println("Network issues")
                }
        )
    }

    var meanStarNumber: Float = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)
        get() = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)

    companion object {

        @get: Exclude
        val initialCategories = arrayOf(
                R.string.initial_category_honest,
                R.string.initial_category_loyal,
                R.string.initial_category_smart,
                R.string.initial_category_attractive,
                R.string.initial_category_authentic,
                R.string.initial_category_generous,
                R.string.initial_category_kind
        )
    }

}