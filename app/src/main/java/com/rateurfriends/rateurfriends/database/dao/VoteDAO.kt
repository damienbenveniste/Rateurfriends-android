package com.rateurfriends.rateurfriends.database.dao

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.Feed
import com.rateurfriends.rateurfriends.models.Vote
import com.rateurfriends.rateurfriends.models.User

class VoteDAO {

    companion object {

        private var instance: VoteDAO? = null

        @Synchronized
        fun getInstance(): VoteDAO {
            if (instance == null) {
                instance = VoteDAO()
            }
            return instance!!
        }

        fun updateVoteForUser(rating: Int,
                              userId: String,
                              contactId: String,
                              categoryName: String,
                              onSuccess: () -> Unit,
                              onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()

            if (userId.isEmpty() || contactId.isEmpty() || categoryName.isEmpty()) {
                println(userId)
                println(contactId)
                println(categoryName)
                return
            }

            val voteRef = db.collection("Vote")
                    .document(userId)
                    .collection(contactId)
                    .document(categoryName)

            val categoryRef = db
                    .collection("Category")
                    .document(categoryName)
                    .collection("User")
                    .document(contactId)

            val userCategoryRef = db
                    .collection("UserAttribute")
                    .document(contactId)
                    .collection("Category")
                    .document(categoryName)

            val userRef = db
                    .collection("User")
                    .document(contactId)

            db.runTransaction { transaction ->

                val snapshotVote = transaction.get(voteRef)
                val snapshotCategory = transaction.get(categoryRef)
                val snapshotUser = transaction.get(userRef)

                if (snapshotCategory.exists() && snapshotUser.exists()) {

                    val previousRating = snapshotVote.get("rating", Int::class.java)

                    val category = snapshotCategory.toObject(Category::class.java)!!
                    val contact = snapshotUser.toObject(User::class.java)!!

                    val countryCategoryRef = db
                            .collection("CategoryCountry")
                            .document(contact.country)
                            .collection("Category")
                            .document(categoryName)
                            .collection("User")
                            .document(contact.userId)

                    val snapshotCountry = transaction.get(countryCategoryRef)

                    if (previousRating != null) {

                        val increment = rating - previousRating
                        val meanStarNumber = (category.starNumber + increment).toDouble() /
                                category.voteNumber.toDouble()
                        val totalMeanStarNumber = (contact.totalStarNumber + increment).toDouble() /
                                contact.totalVoteNumber.toDouble()

                        transaction.update(voteRef, "rating", rating)

                        transaction.update(categoryRef,
                                mapOf(
                                        "starNumber" to FieldValue.increment(increment.toLong()),
                                        "meanStarNumber" to meanStarNumber
                                ))

                        transaction.update(userCategoryRef,
                                mapOf(
                                        "starNumber" to FieldValue.increment(increment.toLong()),
                                        "meanStarNumber" to meanStarNumber
                                ))

                        transaction.update(userRef,
                                mapOf(
                                        "totalStarNumber" to FieldValue.increment(
                                                increment.toLong()),
                                        "meanStarNumber" to totalMeanStarNumber
                                ))

                        if (contact.country.isNotEmpty()) {

                            if (snapshotCountry.exists()) {

                                transaction.update(countryCategoryRef,
                                        mapOf(
                                                "starNumber" to FieldValue.increment(
                                                        increment.toLong()),
                                                "meanStarNumber" to meanStarNumber
                                        ))
                            }


                        }

                    } else {

                        val newVote = Vote(
                                rating = rating,
                                userId = userId,
                                contactId = contactId,
                                categoryName = categoryName)

                        val increment = rating
                        val meanStarNumber = (category.starNumber + increment).toDouble() /
                                (category.voteNumber + 1).toDouble()
                        val totalMeanStarNumber = (contact.totalStarNumber + increment).toDouble() /
                                (contact.totalVoteNumber + 1).toDouble()

                        transaction.set(voteRef, newVote)

                        transaction.update(categoryRef,
                                mapOf(
                                        "starNumber" to FieldValue.increment(increment.toLong()),
                                        "voteNumber" to FieldValue.increment(1),
                                        "meanStarNumber" to meanStarNumber
                                ))

                        transaction.update(userCategoryRef,
                                mapOf(
                                        "starNumber" to FieldValue.increment(increment.toLong()),
                                        "voteNumber" to FieldValue.increment(1),
                                        "meanStarNumber" to meanStarNumber
                                ))

                        transaction.update(userRef,
                                mapOf(
                                        "totalStarNumber" to FieldValue.increment(
                                                increment.toLong()),
                                        "totalVoteNumber" to FieldValue.increment(1),
                                        "meanStarNumber" to totalMeanStarNumber
                                ))

                        val user = Globals.getInstance().user
                        if (user != null) {

                            val feedId = contactId + "_" + (System.currentTimeMillis() / 1000L).toString()

                            val feed = Feed(
                                    feedId = feedId,
                                    userId = user.userId,
                                    userName = user.userName,
                                    categoryName = category.categoryName,
                                    rating = rating,
                                    feedType = "vote_added"
                            )
                            val feedRef = db.collection("UserAttribute")
                                    .document(contactId)
                                    .collection("Feed")
                                    .document(feed.feedId)

                            transaction.set(feedRef, feed)
                        }


                        if (contact.country.isNotEmpty()) {

                            if (snapshotCountry.exists()) {
                                transaction.update(countryCategoryRef,
                                        mapOf(
                                                "starNumber" to FieldValue.increment(
                                                        increment.toLong()),
                                                "voteNumber" to FieldValue.increment(1),
                                                "meanStarNumber" to meanStarNumber
                                        ))
                            }
                        }
                    }

                }

                null
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                onFailure()
            }


        }

        fun getVote(userId: String,
                    contactId: String,
                    categoryName: String,
                    onSuccess: (Vote) -> Unit,
                    onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()

            if (userId.isNotEmpty() && contactId.isNotEmpty() && categoryName.isNotEmpty()) {

                db.collection("Vote")
                        .document(userId)
                        .collection(contactId)
                        .document(categoryName)
                        .get()
                        .addOnSuccessListener {
                            if (it.exists() && it != null) {
                                onSuccess(it.toObject(Vote::class.java)!!)
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }
            }



        }
    }



}