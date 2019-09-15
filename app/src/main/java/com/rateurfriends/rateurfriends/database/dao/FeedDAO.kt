package com.rateurfriends.rateurfriends.database.dao

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Feed

class FeedDAO {

    companion object {

        private var instance: FeedDAO? = null

        @Synchronized
        fun getInstance(): FeedDAO {
            if (instance == null) {
                instance = FeedDAO()
            }
            return instance!!
        }

        fun getTopFeed(callback: (List<Feed>) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val country = Globals.getInstance().user?.country

            if (country != null && country.isNotEmpty()) {
                db.collection("Feed")
                        .whereEqualTo("country", country)
                        .orderBy("timeStamp", Query.Direction.DESCENDING)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty && snapshot != null) {
                                callback(snapshot.map { it.toObject(Feed::class.java) })
                            }
                        }
            } else {

                db.collection("Feed")
                        .orderBy("timeStamp", Query.Direction.DESCENDING)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty && snapshot != null) {
                                callback(snapshot.map { it.toObject(Feed::class.java) })
                            }
                        }

            }
        }

        fun getTopFeedForUser(userId: String, callback: (List<Feed>) -> Unit) {

            val db = FirebaseFirestore.getInstance()

            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Feed")
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty && snapshot != null) {
                            callback(snapshot.map { it.toObject(Feed::class.java) })
                        }
                    }
        }

    }
}