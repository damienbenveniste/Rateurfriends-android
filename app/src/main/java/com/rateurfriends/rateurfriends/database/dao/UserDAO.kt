package com.rateurfriends.rateurfriends.database.dao

import android.app.Activity
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.Contact
import com.rateurfriends.rateurfriends.models.User
import kotlin.math.max

class UserDAO {

    companion object {

        private var instance: UserDAO? = null

        @Synchronized
        fun getInstance(): UserDAO {
            if (instance == null) {
                instance = UserDAO()
            }
            return instance!!
        }

        fun transferStarsForUser(userId: String,
                                 starIncrement: Int,
                                 category: Category,
                                 onSuccess: () -> Unit, onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val categoryRef = db
                    .collection("Category")
                    .document(category.categoryName)
                    .collection("User")
                    .document(userId)

            val userCategoryRef = db
                    .collection("UserAttribute")
                    .document(userId)
                    .collection("Category")
                    .document(category.categoryName)

            val userRef = db
                    .collection("User")
                    .document(userId)

            db.runTransaction { transaction ->

                val snapshotCategory = transaction.get(categoryRef)
                val snapshotUser = transaction.get(userRef)

                if (snapshotCategory.exists()  && snapshotUser.exists()) {

                    val oldCategory = snapshotCategory.toObject(Category::class.java)!!
                    val user = snapshotUser.toObject(User::class.java)!!

                    val meanStarNumber = (oldCategory.starNumber + starIncrement).toDouble() /
                            oldCategory.voteNumber.toDouble()
                    val totalMeanStarNumber = (user.totalStarNumber + starIncrement).toDouble() /
                            user.totalVoteNumber.toDouble()

                    val newSpareStars = max(0, user.spareStars.minus(starIncrement))

                    transaction.update(categoryRef,
                            mapOf(
                                    "starNumber" to FieldValue.increment(starIncrement.toLong()),
                                    "meanStarNumber" to meanStarNumber
                            ))

                    transaction.update(userCategoryRef,
                            mapOf(
                                    "starNumber" to FieldValue.increment(starIncrement.toLong()),
                                    "meanStarNumber" to meanStarNumber
                            ))

                    transaction.update(userRef,
                            mapOf(
                                    "totalStarNumber" to FieldValue.increment(
                                            starIncrement.toLong()),
                                    "meanStarNumber" to totalMeanStarNumber,
                                    "spareStars" to newSpareStars

                            ))

                    if (user.country.isNotEmpty()) {

                        val countryCategoryRef = db
                                .collection("Country")
                                .document(user.country)
                                .collection("Category")
                                .document(oldCategory.categoryName)
                                .collection("User")
                                .document(user.userId)

                        transaction.update(countryCategoryRef,
                                mapOf(
                                        "starNumber" to FieldValue.increment(
                                                starIncrement.toLong()),
                                        "meanStarNumber" to meanStarNumber
                                ))
                    }
                }
                null
            }.addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {
                println("Exception")
                println(it)
                onFailure()
            }

        }

        fun insertUser(user: User,
                       categoryList: List<Category>,
                       activity: Activity,
                       callback: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            val userRef = db
                    .collection("User")
                    .document(user.userId)

            val refAttribute = db
                    .collection("UserAttribute")
                    .document(user.userId)
                    .collection("Category")

            val refCategory = db
                    .collection("Category")

            batch.set(userRef, user)

            categoryList.forEach {
                batch.set(refAttribute.document(it.categoryName), it)
                batch.set(refCategory
                                .document(it.categoryName)
                                .collection("User")
                                .document(user.userId), it)

                batch.set(
                        refCategory.document(it.categoryName),
                        mapOf( "count" to FieldValue.increment(1)),
                        SetOptions.merge()
                )

                if (user.country.isNotEmpty()) {
                    val countryCategoryRef = db
                            .collection("Country")
                            .document(user.country)
                            .collection("Category")
                            .document(it.categoryName)
                            .collection("User")
                            .document(user.userId)

                    batch.set(countryCategoryRef, it)

                    batch.set(
                            db.collection("Country")
                                    .document(user.country)
                                    .collection("Category")
                                    .document(it.categoryName),
                            mapOf( "count" to FieldValue.increment(1)),
                            SetOptions.merge()
                    )
                }
            }


            batch.commit()
                    .addOnSuccessListener { callback() }
                    .addOnFailureListener {
                        Toast.makeText(
                                activity,
                                activity.getString(R.string.information_not_saved),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
        }


        fun incrementSpareStarsForUser(userId: String, increment: Int, callback: () -> Unit) {

            // TODO: there does not seem to be a need to keep in sync with Country/country/User
            val db = FirebaseFirestore.getInstance()

            db.collection("User")
                    .document(userId)
                    .update("spareStars", FieldValue.increment(increment.toLong()))
                    .addOnSuccessListener {
                        callback()
                    }

        }

        fun incrementSpareCategoriesForUser(userId: String, increment: Int, callback: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .update("spareCategories", FieldValue.increment(increment.toLong()))
                    .addOnSuccessListener {
                        callback()
                    }

        }

        fun getUser(userId: String, callback: (User) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            callback(document.toObject(User::class.java)!!)
                        }
                    }

        }

        fun insertNewContact(contact: Contact, userId: String) {

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Contact")
                    .document(contact.userId)
                    .set(contact)

            val user = Globals.getInstance().user

            if (user != null) {

                val userContact = Contact(
                        phoneName="",
                        phoneNumber=user.phoneNumber,
                        userId=user.userId
                )

                db.collection("UserAttribute")
                        .document(contact.userId)
                        .collection("Contact")
                        .document(userContact.userId)
                        .set(userContact)
            }
        }

        fun insertContactListForUser(contactList: List<Contact>, userId: String) {
            contactList.forEach { insertNewContact(it, userId) }
        }

        fun getContactsForUser(userId: String, callback: (QuerySnapshot) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Contact")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null && !documents.isEmpty) {
                            callback(documents)
                        }
                    }
                    .addOnFailureListener {
                        println("Failure")
                    }
        }

        fun checkUserExistWithPhone(phoneNumber: String, callback: (QuerySnapshot) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .whereEqualTo("phoneNumber", phoneNumber)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null) {
                            callback(documents)
                        }
                    }
        }

        fun checkUserExistWithUserId(userId: String, callback: (Boolean) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .get()
                    .addOnSuccessListener {
                        callback(it.exists())
                    }.addOnFailureListener {
                        println("Could not check if exits")
                        println(it)
                    }
        }

        fun updateUser(userId: String, params: Map<String, Any?>, callback: (User) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("User")
                    .document(userId)

            db.runTransaction {

                val user = it.get(userRef).toObject(User::class.java)

                if (user != null) {

                    it.update(userRef, params)
                }
                user
            }.addOnSuccessListener {
                callback(it)
            }.addOnFailureListener {
                println("Could not update")
                println(it)
            }
        }

        fun getUsersBy(orderByCount: Boolean,
                       descending: Boolean,
                       local: Boolean,
                       callback: (List<User>) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val direction = if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
            val orderBy = if (orderByCount) "totalStarNumber" else "meanStarNumber"

            val country = Globals.getInstance().user?.country

            if (local && country != null && country.isNotEmpty()) {

                db.collection("User")
                        .whereEqualTo("country", country)
                        .orderBy(orderBy, direction)
                        .limit(100)
                        .get()
                        .addOnSuccessListener {
                            if (!it.isEmpty && it != null) {
                                callback(it.map{u -> u.toObject(User::class.java)})
                            }
                        }

            } else {

                db.collection("User")
                        .orderBy(orderBy, direction)
                        .limit(100)
                        .get()
                        .addOnSuccessListener {
                            if (!it.isEmpty && it != null) {
                                callback(it.map{u -> u.toObject(User::class.java)})
                            }
                        }

            }
        }

        fun sendInvite(userId: String, contact: Contact, callback: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()
            val inviteRef = db.collection("UserAttribute")
                    .document(userId)
                    .collection("Invite")
                    .document(contact.phoneNumber)

            val userRef = db.collection("User")
                    .document(userId)

            batch.set(inviteRef, contact)
            batch.update(userRef, "spareCategories", FieldValue.increment(1))

            batch.commit().addOnSuccessListener {
                callback()
            }
        }

        fun getInvitedContactsForUser(userId: String, callback: (List<Contact>) -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Invite")
                    .get()
                    .addOnCompleteListener {
                        var contactList: List<Contact> = listOf()
                        if (it.isSuccessful) {
                            val snapshot = it.result!!
                            if (!snapshot.isEmpty) {
                                contactList = snapshot.map { doc ->
                                    doc.toObject(Contact::class.java)
                                }
                            }
                        }
                        callback(contactList)
                    }


        }
    }

}
