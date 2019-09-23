package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.CategorySearchAdapter
import com.rateurfriends.rateurfriends.adapters.ProfileCategoryAdapter
import com.rateurfriends.rateurfriends.customViews.IntegerButton
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.fragments.ProfileFragment
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Category
import android.widget.EditText
import com.rateurfriends.rateurfriends.database.dao.FeedDAO


class ProfileController(
        val fragment: ProfileFragment
) {
    private val REQUEST_CAMERA = 79
    private val REQUEST_UPLOAD = 78
    private val pictureController = PictureController(fragment)
    private val categoryList: ArrayList<Category> = arrayListOf()
    private var categoriesAdapter: ProfileCategoryAdapter? = null
    private var spareStars: Int = 0
    private var currentUserSpareStars = Globals.getInstance().user?.spareStars

    private val textWatcher: TextWatcher = object: TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchCategory(s!!, fragment.categoryNameList, fragment.categorySearchAdapter!!)
        }

    }

    inner class CustomTextWatcher(private val integerButton: IntegerButton) : TextWatcher {

        override fun afterTextChanged(s: Editable) {
            // Unregister self before update
            var value = 0
            if (s.isNotEmpty()) {
                value = maxOf(s.toString().toInt(), 0)
            }

            if (currentUserSpareStars!! + spareStars - value < 0) {
                integerButton.integerEditText.removeTextChangedListener(this)
                integerButton.integerEditText.setText(spareStars.toString())
                integerButton.integerEditText.addTextChangedListener(this)
            } else {
                currentUserSpareStars = currentUserSpareStars!! + (spareStars - value)
                integerButton.plusButton.isEnabled = currentUserSpareStars!! > 0

                fragment.spareStarsTextView!!.text = fragment
                        .getString(R.string.profile_spare_stars_text_view)
                        .format(currentUserSpareStars)

                spareStars = value
            }
        }

        override fun beforeTextChanged(s: CharSequence,
                                       start: Int,
                                       count: Int,
                                       after: Int) {
            var value = 0
            if (s.isNotEmpty()) {
                value = maxOf(s.toString().toInt(), 0)
            }

            spareStars = value
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    fun onCategoryClicked(layout: FrameLayout, integerButton: IntegerButton) {

        if (layout.visibility == View.GONE && currentUserSpareStars != null) {
            layout.visibility = View.VISIBLE

            integerButton.integerEditText.addTextChangedListener(
                    CustomTextWatcher(integerButton))
        }
    }


    fun setUpProfile() {

        val user = Globals.getInstance().user
        val profileImageView = fragment.profileImageView
        val levelView = fragment.levelView
        val levelTextView = fragment.levelTextView
        val userNameTextView = fragment.userNameTextView
        val startNumberTextView = fragment.startNumberTextView
        val meanStarTextView = fragment.meanStarTextView
        val ratingBar = fragment.ratingBar
        val spareCategoriesTextView = fragment.spareCategoriesTextView
        val spareStarsTextView = fragment.spareStarsTextView

        if (profileImageView == null ||
                levelView == null ||
                levelTextView == null ||
                userNameTextView == null ||
                startNumberTextView == null ||
                meanStarTextView == null ||
                ratingBar == null ||
                spareCategoriesTextView == null ||
                spareStarsTextView == null)  {
            return
        }

        pictureController.populateImageView(profileImageView)

        if (user != null) {
            levelView.levelText = user.level
            userNameTextView.text = user.userName.capitalize()

            levelTextView.text = fragment
                    .getString(R.string.profile_level_text_view)
                    .format(user.levelNumber())

            startNumberTextView.text = fragment
                    .getString(R.string.star_number_format)
                    .format(user.totalStarNumber)

            meanStarTextView.text = fragment
                    .getString(R.string.mean_star_format)
                    .format(user.meanStarNumber)

            spareCategoriesTextView.text = fragment
                    .getString(R.string.profile_spare_categories_text_view)
                    .format(user.spareCategories)

            spareStarsTextView.text = fragment
                    .getString(R.string.profile_spare_stars_text_view)
                    .format(user.spareStars)

            ratingBar.rating = user.meanStarNumber

        } else {

            Globals.getInstance().setUser {
                levelView.levelText = it.level

                userNameTextView.text = it.userName.capitalize()
                levelTextView.text = fragment
                        .getString(R.string.profile_level_text_view)
                        .format(it.levelNumber())

                startNumberTextView.text = fragment
                        .getString(R.string.star_number_format)
                        .format(it.totalStarNumber)

                meanStarTextView.text = fragment
                        .getString(R.string.mean_star_format)
                        .format(it.meanStarNumber)

                spareCategoriesTextView.text = fragment
                        .getString(R.string.profile_spare_categories_text_view)
                        .format(it.spareCategories)

                spareStarsTextView.text = fragment
                        .getString(R.string.profile_spare_stars_text_view)
                        .format(it.spareStars)

                ratingBar.rating = it.meanStarNumber
            }
        }
    }

    fun getAddCategoryForm() {
        val visibility =  fragment.addCategoryLayout?.visibility

        if (visibility == View.GONE) {
            fragment.addCategoryLayout?.visibility = View.VISIBLE
        } else if (visibility == View.VISIBLE) {
            fragment.addCategoryLayout?.visibility = View.GONE
        }
    }

    fun removeView(layout: FrameLayout) {
        layout.visibility = View.GONE
    }

    private fun searchCategory(
            query: CharSequence,
            categoryNameList: ArrayList<String>,
            adapter: CategorySearchAdapter
    ) {
        categoryNameList.clear()
        if (query.isNotEmpty()) {
            CategoryDAO.queryCategory(query.toString().toLowerCase(),
                    onSuccess = {
                        for (document in it) {
                            categoryNameList.add(document.id)
                        }
                        adapter.notifyDataSetChanged()
                    },
                    onFailure = {
                        Toast.makeText(
                                fragment.context,
                                fragment.getString(R.string.hall_of_fame_problem_finding_qualities),
                                Toast.LENGTH_SHORT
                        ).show()
                    }
            )
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    fun showPictureEditView(layout: FrameLayout) {
        layout.visibility = View.VISIBLE
    }

    fun checkPermissionForCameraView() {

        if (ActivityCompat.checkSelfPermission(
                        fragment.activity!!,
                        android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {

            pictureController.getCameraView()
        } else {
            ActivityCompat.requestPermissions(
                    fragment.activity!!,
                    arrayOf(android.Manifest.permission.CAMERA),
                    REQUEST_CAMERA
            )
        }
    }

    fun checkPermissionForPictureUpload() {

        if (ActivityCompat.checkSelfPermission(
                        fragment.activity!!,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED) {

            pictureController.getImageChooserView()

        } else {
            ActivityCompat.requestPermissions(
                    fragment.activity!!,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_UPLOAD
            )
        }
    }

    fun handlePermission(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pictureController.getCameraView()
                } else {
                    Toast.makeText(
                            fragment.activity!!,
                            fragment.getString(R.string.no_camera_use),
                            Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
            REQUEST_UPLOAD -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pictureController.getImageChooserView()
                } else {
                    Toast.makeText(
                            fragment.activity!!,
                            fragment.getString(R.string.no_upload_use),
                            Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    fun onCategorySelected(categoryName: String) {

        fragment.addCategoryEditText!!.removeTextChangedListener(textWatcher)
        fragment.addCategoryEditText!!.setText(categoryName)
        fragment.categoryNameList.clear()
        fragment.categorySearchAdapter!!.notifyDataSetChanged()
        hideKeyboardFrom(fragment.context!!, fragment.addCategoryEditText!!)
        fragment.addCategoryEditText!!.addTextChangedListener(textWatcher)
    }

    private fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun onActivityResult(requestCode: Int,
                         resultCode: Int,
                         data: Intent?,
                         profileImageView: ImageView) {

        removeView(fragment.editPictureLayout!!)
        pictureController.processCapturedPhoto(requestCode, resultCode, data, profileImageView)

    }

    fun getCategories(rvCategories: RecyclerView) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        CategoryDAO.getCategoriesForUser(userId,
                onSuccess = {
                    fragment.progressLayout!!.visibility = View.GONE

                    for (doc in it) {
                        val category = doc.toObject(Category::class.java)
                        categoryList.add(category)
                    }

                    categoriesAdapter = ProfileCategoryAdapter(userId, categoryList, fragment)
                    rvCategories.adapter = categoriesAdapter
                    rvCategories.layoutManager = LinearLayoutManager(fragment.activity!!)
                },
                onFailure = {
                    fragment.progressLayout!!.visibility = View.GONE
                    Toast.makeText(
                            fragment.activity!!,
                            fragment.getString(R.string.contact_profile_could_not_get_qualities),
                            Toast.LENGTH_LONG
                    ).show()
                },
                onEmpty = {
                    fragment.progressLayout!!.visibility = View.GONE
                    Toast.makeText(
                            fragment.activity!!,
                            fragment.getString(R.string.profile_no_quality),
                            Toast.LENGTH_LONG
                    ).show()
                }
        )
    }

    fun getTextChangedListener(): TextWatcher {
        return textWatcher
    }

    fun submitCategory() {

        val addCategoryEditText = fragment.addCategoryEditText
        val addCategoryLayout = fragment.addCategoryLayout
        val spareCategoriesTextView = fragment.spareCategoriesTextView

        if (addCategoryEditText == null ||
                addCategoryLayout == null ||
                spareCategoriesTextView == null ||
                Globals.getInstance().user == null) {
            return
        }

        if (validateCategory(addCategoryEditText) &&
                Globals.getInstance().user!!.spareCategories > 0) {

            val category = Category(
                    addCategoryEditText.text.toString().toLowerCase().trim(),
                    Globals.getInstance().user!!.userId
            )

            fragment.progressLayout!!.visibility = View.VISIBLE

            CategoryDAO.insertCategoryForUser(category, Globals.getInstance().user!!,
                    onSuccess = {
                        val increment = -1
                        UserDAO.incrementSpareCategoriesForUser(
                                Globals.getInstance().user!!.userId,
                                increment,
                                onSuccess = {
                                    fragment.progressLayout!!.visibility = View.GONE

                                    categoryList.add(0, category)
                                    categoriesAdapter!!.notifyItemInserted(0)
                                    addCategoryLayout.visibility = View.GONE

                                    Globals.getInstance().user!!.spareCategories += increment

                                    spareCategoriesTextView.text = fragment
                                            .getString(R.string.profile_spare_categories_text_view)
                                            .format(Globals.getInstance().user!!.spareCategories)

                                    if (Globals.getInstance().user!!.spareCategories <= 0) {
                                        fragment.submitCategoryButton!!.isEnabled = false
                                    }
                                },
                                onFailure = {
                                    fragment.progressLayout!!.visibility = View.GONE
                                    println("Could not remove category")
                                }
                        )

                        FeedDAO.addCategoryFeed(category.categoryName)

                    },
                    onFailure = {
                        fragment.progressLayout!!.visibility = View.GONE
                        Toast.makeText(
                                fragment.activity!!,
                                fragment.getString(R.string.profile_could_not_insert_quality),
                                Toast.LENGTH_LONG
                        ).show()

                    },
                    onKnownCategory = {
                        fragment.progressLayout!!.visibility = View.GONE
                        Toast.makeText(
                                fragment.activity!!,
                                fragment.getString(R.string.profile_known_quality),
                                Toast.LENGTH_LONG
                        ).show()

                    }
            )
        } else {
            val categoryName = addCategoryEditText.text.toString().toLowerCase().trim()
            if (categoryName in categoryList.map {it.categoryName.toLowerCase()}) {
                Toast.makeText(
                        fragment.activity!!,
                        fragment.getString(R.string.profile_known_quality),
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun validateCategory(editText: EditText): Boolean {

        if (editText.text.isNotEmpty()) {
            val categoryName = editText.text.toString().toLowerCase().trim()
            return categoryName !in categoryList.map {it.categoryName.toLowerCase()}
        }
        return false
    }

}