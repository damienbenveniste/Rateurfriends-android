package com.rateurfriends.rateurfriends.database.dao

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Contact
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

        fun getTopFeed(
                onSuccess: (List<Feed>) -> Unit,
                onFailure: () -> Unit,
                onEmpty: () -> Unit) {

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
                                onSuccess(snapshot.map { it.toObject(Feed::class.java) })
                            } else {
                                onEmpty()
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }
            } else {

                db.collection("Feed")
                        .orderBy("timeStamp", Query.Direction.DESCENDING)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty && snapshot != null) {
                                onSuccess(snapshot.map { it.toObject(Feed::class.java) })
                            } else {
                                onEmpty()
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }

            }
        }

        fun getTopFeedForUser(userId: String,
                              onSuccess: (List<Feed>) -> Unit,
                              onFailure: () -> Unit,
                              onEmpty: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()

            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Feed")
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty && snapshot != null) {
                            onSuccess(snapshot.map { it.toObject(Feed::class.java) })
                        } else {
                            onEmpty()
                        }
                    }.addOnFailureListener {
                        onFailure()
                    }
        }

        fun addCategoryFeed(categoryName: String) {

            if (categoryName.isEmpty()) {
                return
            }

            val db = FirebaseFirestore.getInstance()

            val user = Globals.getInstance().user

            if (user != null) {

                db.collection("UserAttribute")
                        .document(user.userId)
                        .collection("Contact")
                        .get()
                        .addOnSuccessListener {
                            if (!it.isEmpty && it != null) {
                                val contactList = it.map { doc -> doc.toObject(Contact::class.java) }

                                for (contact in contactList) {

                                    val feedId = user.userId + "_" + (System.currentTimeMillis() / 1000L).toString()

                                    db.collection("UserAttribute")
                                            .document(contact.userId)
                                            .collection("Contact")
                                            .document(user.userId)
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                if (snapshot.exists()) {

                                                    val feed = Feed(
                                                            feedId = feedId,
                                                            userId = user.userId,
                                                            userName = user.userName,
                                                            categoryName = categoryName,
                                                            feedType = "category_added"
                                                    )

                                                    db.collection("UserAttribute")
                                                            .document(contact.userId)
                                                            .collection("Feed")
                                                            .document(feedId)
                                                            .set(feed)

                                                }
                                            }
                                }
                            }
                        }
            }

        }

    }
}