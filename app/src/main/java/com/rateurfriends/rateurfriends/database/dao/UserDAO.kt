package com.rateurfriends.rateurfriends.database.dao

import android.app.Activity
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.protobuf.Empty
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
                                 onSuccess: () -> Unit,
                                 onFailure: () -> Unit) {

            if (userId.isEmpty() || category.categoryName.isEmpty()) {
                onFailure()
                return
            }

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
                                .collection("CategoryCountry")
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
                onFailure()
            }

        }

        fun insertUser(user: User,
                       categoryList: List<Category>,
                       onSuccess: () -> Unit,
                       onFailure: () -> Unit) {

            if (user.userId.isEmpty() || categoryList.map { it.categoryName.isEmpty()}.any()) {
                onFailure()
                return
            }

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
                            .collection("CategoryCountry")
                            .document(user.country)
                            .collection("Category")
                            .document(it.categoryName)
                            .collection("User")
                            .document(user.userId)

                    batch.set(countryCategoryRef, it)

                    batch.set(
                            db.collection("CategoryCountry")
                                    .document(user.country)
                                    .collection("Category")
                                    .document(it.categoryName),
                            mapOf( "count" to FieldValue.increment(1)),
                            SetOptions.merge()
                    )
                }
            }


            batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        onFailure()
                    }
        }


        fun incrementSpareStarsForUser(userId: String,
                                       increment: Int,
                                       onSuccess: () -> Unit,
                                       onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            // TODO: there does not seem to be a need to keep in sync with Country/country/User
            val db = FirebaseFirestore.getInstance()

            db.collection("User")
                    .document(userId)
                    .update("spareStars", FieldValue.increment(increment.toLong()))
                    .addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        onFailure()
                    }

        }

        fun incrementSpareCategoriesForUser(userId: String,
                                            increment: Int,
                                            onSuccess: () -> Unit,
                                            onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .update("spareCategories", FieldValue.increment(increment.toLong()))
                    .addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        onFailure()
                    }

        }

        fun getUser(userId: String,
                    onSuccess: (User) -> Unit,
                    onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            onSuccess(document.toObject(User::class.java)!!)
                        }
                    }.addOnFailureListener {
                        onFailure()
                    }

        }

        fun insertNewContact(contact: Contact,
                             userId: String,
                             onFailure: () -> Unit) {

            if (contact.userId.isEmpty() || userId.isEmpty()) {
                onFailure()
                return
            }

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
                        .addOnFailureListener {
                            onFailure()
                        }
            }
        }

        fun getContactsForUser(userId: String,
                               onSuccess: (QuerySnapshot) -> Unit,
                               onFailure: () -> Unit,
                               onEmpty: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Contact")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null && !documents.isEmpty) {
                            onSuccess(documents)
                        } else {
                            onEmpty()
                        }
                    }
                    .addOnFailureListener {
                        onFailure()
                    }
        }

        fun checkUserExistWithPhone(phoneNumber: String,
                                    onSuccess: (QuerySnapshot) -> Unit,
                                    onFailure: () -> Unit) {

            if (phoneNumber.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .whereEqualTo("phoneNumber", phoneNumber)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents != null) {
                            onSuccess(documents)
                        }
                    }.addOnFailureListener {
                        onFailure()
                    }
        }

        fun checkUserExistWithUserId(userId: String, onSuccess: (Boolean) -> Unit,
                                     onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("User")
                    .document(userId)
                    .get()
                    .addOnSuccessListener {
                        onSuccess(it.exists())
                    }.addOnFailureListener {
                        onFailure()
                    }
        }

        fun updateUser(userId: String,
                       params: Map<String, Any?>,
                       onSuccess: (User) -> Unit,
                       onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

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
                onSuccess(it)
            }.addOnFailureListener {
                onFailure()
            }
        }

        fun getUsersBy(orderByCount: Boolean,
                       descending: Boolean,
                       local: Boolean,
                       onSuccess: (List<User>) -> Unit,
                       onFailure: () -> Unit) {

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
                                onSuccess(it.map{u -> u.toObject(User::class.java)})
                            }
                        }.addOnFailureListener {
                            println(it)
                            onFailure()
                        }

            } else {

                db.collection("User")
                        .orderBy(orderBy, direction)
                        .limit(100)
                        .get()
                        .addOnSuccessListener {
                            if (!it.isEmpty && it != null) {
                                onSuccess(it.map{u -> u.toObject(User::class.java)})
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }

            }
        }

        fun sendInvite(userId: String,
                       contact: Contact,
                       onSuccess: () -> Unit,
                       onFailure: () -> Unit) {

            if (userId.isEmpty() || contact.userId.isEmpty()) {
                onFailure()
                return
            }

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
                onSuccess()
            }.addOnFailureListener {
                onFailure()
            }
        }

        fun getInvitedContactsForUser(userId: String,
                                      onSuccess: (List<Contact>) -> Unit,
                                      onFailure: () -> Unit) {

            if (userId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Invite")
                    .get()
                    .addOnSuccessListener {
                        var contactList: List<Contact> = listOf()
                        if (!it.isEmpty && it != null) {
                            contactList = it.map { doc ->
                                doc.toObject(Contact::class.java)
                            }
                        }
                        onSuccess(contactList)
                    }. addOnFailureListener {
                        onFailure()
                    }
        }

    }

}
