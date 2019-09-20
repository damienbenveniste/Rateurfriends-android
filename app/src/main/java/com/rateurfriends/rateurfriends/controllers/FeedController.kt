package com.rateurfriends.rateurfriends.controllers

import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.FeedDAO
import com.rateurfriends.rateurfriends.fragments.FeedFragment
import com.rateurfriends.rateurfriends.models.User

class FeedController(
        private val fragment: FeedFragment
) {

    private fun getOverallFeed() {
        fragment.feedList.clear()
        FeedDAO.getTopFeed(
                onSuccess = { feeds ->
                    feeds.forEach {
                        fragment.feedList.add(it)
                    }
                    fragment.feedAdapter!!.notifyDataSetChanged()
                    fragment.progressLayout!!.visibility = View.GONE
                },
                onFailure = {
                    fragment.progressLayout!!.visibility = View.GONE
                    Toast.makeText(
                            fragment.context,
                            fragment.getString(R.string.feed_could_not_get_feeds),
                            Toast.LENGTH_SHORT
                    ).show()

                }

        )
    }

    private fun getFeedForUser() {
        val userId = FirebaseAuth.getInstance()!!.currentUser!!.uid
        fragment.feedList.clear()
        FeedDAO.getTopFeedForUser(userId,
                onSuccess = { feeds ->

                    feeds.forEach {
                        fragment.feedList.add(it)
                    }
                    fragment.feedAdapter!!.notifyDataSetChanged()
                    fragment.progressLayout!!.visibility = View.GONE

                },
                onFailure = {
                    fragment.progressLayout!!.visibility = View.GONE
                    Toast.makeText(
                            fragment.context,
                            fragment.getString(R.string.feed_could_not_get_feeds),
                            Toast.LENGTH_SHORT
                    ).show()

                }
        )
    }

    fun changeFeedState() {

        if (fragment.feedButton!!.isChecked) {
            getFeedForUser()
        } else {
            getOverallFeed()
        }

    }


}