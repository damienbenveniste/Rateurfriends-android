package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.Context
import android.view.View
//import android.support.v4.app.Fragment
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.rateurfriends.rateurfriends.ContactProfileActivity
import com.rateurfriends.rateurfriends.adapters.ContactCategoryAdapter
import com.rateurfriends.rateurfriends.adapters.ProfileCategoryAdapter
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.database.dao.VoteDAO
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User

class ContactProfileController(
        val activity: ContactProfileActivity
) {

    private val categoryList: ArrayList<Category> = arrayListOf()
    private var categoriesAdapter: ContactCategoryAdapter? = null


    fun setUpProfile() {

        val user = activity.user!!

        PictureDAO.populateImageView(
                user.userId,
                activity.contactImageView!!,
                activity
        )

        activity.nameTextView!!.text = user.userName.capitalize()

        if (activity.phoneName!!.isNotEmpty()) {
            activity.phoneNameTextView!!.text = activity.phoneName!!.capitalize()
        }

        activity.levelView!!.levelText = user.level
        activity.levelTextView!!.text = "Level %d".format(user.levelNumber())
        activity.ratingBar!!.rating = user.meanStarNumber
        activity.meanStarTextView!!.text = "%.1f".format(user.meanStarNumber)
        activity.startNumberTextView!!.text = "★%d".format(user.totalStarNumber)
    }

    fun getCategories(contact: User, rvCategories: RecyclerView) {
        val userId = contact.userId
        activity.progressLayout!!.visibility = View.VISIBLE
        CategoryDAO.getCategoriesForUser(userId) {
            documents ->
            if (!documents.isEmpty) {

                for (doc in documents) {
                    val category = doc.toObject(Category::class.java)
                    if (category.publicVisibility) {
                        categoryList.add(category)
                    }
                }

                categoriesAdapter = ContactCategoryAdapter(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        categoryList,
                        activity,
                        activity.progressLayout!!,
                        activity
                )

                rvCategories.adapter = categoriesAdapter
                rvCategories.layoutManager = LinearLayoutManager(activity)
                activity.progressLayout!!.visibility = View.GONE
            }
        }

    }
}