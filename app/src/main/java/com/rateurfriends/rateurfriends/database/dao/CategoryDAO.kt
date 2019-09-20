package com.rateurfriends.rateurfriends.database.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User
import java.util.HashMap

class CategoryDAO {

    companion object {

        private var instance: CategoryDAO? = null

        @Synchronized
        fun getInstance(): CategoryDAO {
            if (instance == null) {
                instance = CategoryDAO()
            }
            return instance!!
        }


        fun queryCategory(query: String, onSuccess: (List<DocumentSnapshot>) -> Unit,
                          onFailure: () -> Unit) {

            val queryValue = query.toLowerCase()
            val db = FirebaseFirestore.getInstance()

            db.collection("Category")
                    .whereGreaterThanOrEqualTo(FieldPath.documentId(), queryValue)
                    .whereLessThanOrEqualTo(FieldPath.documentId(), queryValue + "z")
                    .limit(100)
                    .get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            onSuccess(it.documents)
                        }
                    }.addOnFailureListener {
                        onFailure()
                    }

        }

        fun insertCategoryForUser(category: Category,
                                  user: User,
                                  onSuccess: () -> Unit,
                                  onFailure: () -> Unit,
                                  onKnownCategory: () -> Unit) {

            val db = FirebaseFirestore.getInstance()

            println(category.categoryName)

            db.collection("UserAttribute")
                    .document(user.userId)
                    .collection("Category")
                    .document(category.categoryName)
                    .get()
                    .addOnSuccessListener {
                        println(it.exists())
                        println(it)
                        if (!it.exists()) {

                            val batch = db.batch()

                            val refAttribute = db.collection("UserAttribute")
                                    .document(user.userId)
                                    .collection("Category")
                                    .document(category.categoryName)

                            val refCategory = db.collection("Category")
                                    .document(category.categoryName)
                                    .collection("User")
                                    .document(user.userId)

                            batch.set(refAttribute, category)
                            batch.set(refCategory, category)

                            batch.set(
                                    db.collection("Category")
                                            .document(category.categoryName),
                                    mapOf( "count" to FieldValue.increment(1)),
                                    SetOptions.merge()
                            )

                            if (user.country.isNotEmpty()) {

                                val countryCategoryRef = db.collection("Country")
                                        .document(user.country)
                                        .collection("Category")
                                        .document(category.categoryName)
                                        .collection("User")
                                        .document(user.userId)

                                batch.set(countryCategoryRef, category)

                                batch.set(
                                        db.collection("Country")
                                                .document(user.country)
                                                .collection("Category")
                                                .document(category.categoryName),
                                        mapOf( "count" to FieldValue.increment(1)),
                                        SetOptions.merge()
                                )
                            }

                            batch.commit()
                                    .addOnSuccessListener {
                                        onSuccess()
                                    }.addOnFailureListener {
                                        onFailure()
                                    }

                        } else {
                            onKnownCategory()
                        }
                    }
        }

        fun getCategoriesForUser(userId: String,
                                 onSuccess: (QuerySnapshot) -> Unit,
                                 onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Category")
                    .get()
                    .addOnSuccessListener {
                        if (!it.isEmpty) {
                            onSuccess(it)
                        }
                    }.addOnFailureListener {
                        onFailure()
                    }
        }

        fun getCategoryForUser(userId: String,
                               categoryName: String,
                               onSuccess: (DocumentSnapshot) -> Unit,
                               onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            db.collection("UserAttribute")
                    .document(userId)
                    .collection("Category")
                    .document(categoryName)
                    .get()
                    .addOnSuccessListener { documents -> onSuccess(documents) }
                    .addOnFailureListener { onFailure() }
        }

        fun setPublicVisibility(category: Category,
                                userId: String,
                                publicVisibility: Boolean,
                                onSuccess: () -> Unit,
                                onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            val refAttribute = db.collection("UserAttribute")
                    .document(userId)
                    .collection("Category")
                    .document(category.categoryName)

            val refCategory = db.collection("Category")
                    .document(category.categoryName)
                    .collection("User")
                    .document(userId)

            batch.update(refAttribute, "publicVisibility", publicVisibility)
            batch.update(refCategory, "publicVisibility", publicVisibility)

            batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener {
                        onFailure()
                    }
        }

        fun getUsersForCategory(category: String,
                                orderByCount: Boolean,
                                descending: Boolean,
                                local: Boolean,
                                onSuccess: (List<Category>) -> Unit,
                                onFailure: () -> Unit) {

            val db = FirebaseFirestore.getInstance()
            val direction = if (descending) Query.Direction.DESCENDING else Query.Direction.ASCENDING
            val orderBy = if (orderByCount) "starNumber" else "meanStarNumber"

            val country = Globals.getInstance().user?.country

            if (local && country != null && country.isNotEmpty()) {

                db.collection("Country")
                        .document(country)
                        .collection("Category")
                        .document(category)
                        .collection("User")
                        .orderBy(orderBy, direction)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty && snapshot != null) {
                                onSuccess(snapshot.map {
                                            it.toObject(Category::class.java)
                                        }
                                )
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }


            } else {

                db.collection("Category")
                        .document(category)
                        .collection("User")
                        .orderBy(orderBy, direction)
                        .limit(100)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty && snapshot != null) {
                                onSuccess(snapshot.map {
                                    it.toObject(Category::class.java)
                                }
                                )
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }

            }

        }

        fun getUsersByCategory(orderByCount: Boolean,
                               descending: Boolean,
                               local: Boolean,
                               onSuccess: (String, List<Category>) -> Unit,
                               onFailure: () -> Unit) {


            val db = FirebaseFirestore.getInstance()
            val direction = if (descending)
                Query.Direction.DESCENDING else
                Query.Direction.ASCENDING

            val orderBy = if (orderByCount) "starNumber" else "meanStarNumber"

            val country = Globals.getInstance().user?.country

            if (local && country != null && country.isNotEmpty()) {

                db.collection("Country")
                        .document(country)
                        .collection("Category")
                        .orderBy("count", Query.Direction.DESCENDING)
                        .limit(20)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            if (!snapshot.isEmpty && snapshot != null) {
                                for (document in snapshot.documents) {
                                    val category = document.id

                                    db.collection("Country")
                                            .document(country)
                                            .collection("Category")
                                            .document(category)
                                            .collection("User")
                                            .orderBy(orderBy, direction)
                                            .limit(20)
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                if (!snapshot.isEmpty && snapshot != null) {
                                                    onSuccess(
                                                            category,
                                                            snapshot.map {
                                                                it.toObject(Category::class.java)
                                                            }
                                                    )
                                                }
                                            }
                                }
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }

            } else {

                db.collection("Category")
                        .orderBy("counts", Query.Direction.DESCENDING)
                        .limit(20)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            if (!snapshot.isEmpty && snapshot != null) {
                                for (document in snapshot.documents) {
                                    val category = document.id

                                    db.collection("Category")
                                            .document(category)
                                            .collection("User")
                                            .orderBy(orderBy, direction)
                                            .limit(20)
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                if (!snapshot.isEmpty && snapshot != null) {
                                                    onSuccess(
                                                            category,
                                                            snapshot.map {
                                                                it.toObject(Category::class.java)
                                                            }
                                                    )
                                                }
                                            }
                                }
                            }
                        }.addOnFailureListener {
                            onFailure()
                        }

            }

        }
    }

}