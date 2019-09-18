package com.rateurfriends.rateurfriends.controllers

import android.view.View
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.database.dao.FeedDAO
import com.rateurfriends.rateurfriends.fragments.FeedFragment
import com.rateurfriends.rateurfriends.models.User

class FeedController(
        private val fragment: FeedFragment
) {

    private fun getOverallFeed() {
        fragment.feedList.clear()
        FeedDAO.getTopFeed { feeds ->

            feeds.forEach {
                fragment.feedList.add(it)
            }
            fragment.feedAdapter!!.notifyDataSetChanged()
            fragment.progressLayout!!.visibility = View.GONE
        }
    }

    private fun getFeedForUser() {
        val userId = FirebaseAuth.getInstance()!!.currentUser!!.uid
        fragment.feedList.clear()
        FeedDAO.getTopFeedForUser(userId) { feeds ->

            feeds.forEach {
                fragment.feedList.add(it)
            }
            fragment.feedAdapter!!.notifyDataSetChanged()
            fragment.progressLayout!!.visibility = View.GONE
        }
    }

    fun changeFeedState() {

        if (fragment.feedButton!!.isChecked) {
            getFeedForUser()
        } else {
            getOverallFeed()
        }

    }


}