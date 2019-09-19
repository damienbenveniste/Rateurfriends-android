package com.rateurfriends.rateurfriends.models


import android.content.res.Resources
import com.google.firebase.firestore.Exclude
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import kotlin.math.max
import kotlin.math.min

class Category(

        // TODO: add user name and user id
        var categoryName: String = "",

        val userId: String = "",

        var publicVisibility: Boolean = true,

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
        CategoryDAO.setPublicVisibility(this, userId, !this.publicVisibility) {
            this.publicVisibility = !this.publicVisibility
        }
    }

    var meanStarNumber: Float = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)
        get() = max(min(starNumber.toFloat() / voteNumber.toFloat(), 5f), 0f)

    companion object {

        @get: Exclude
        val initialCategories = arrayOf(
                Resources.getSystem().getString(R.string.initial_category_honest),
                Resources.getSystem().getString(R.string.initial_category_loyal),
                Resources.getSystem().getString(R.string.initial_category_smart),
                Resources.getSystem().getString(R.string.initial_category_attractive),
                Resources.getSystem().getString(R.string.initial_category_authentic),
                Resources.getSystem().getString(R.string.initial_category_generous),
                Resources.getSystem().getString(R.string.initial_category_kind)
        )
    }

}