package com.rateurfriends.rateurfriends.controllers

//import android.R
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
//import android.widget.ToggleButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.CategorySearchAdapter
import com.rateurfriends.rateurfriends.adapters.CategoryRankingAdapter
import com.rateurfriends.rateurfriends.adapters.UserRankingAdapter
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.fragments.HallOfFameFragment
import com.rateurfriends.rateurfriends.models.Category

class HallOfFameController(val fragment: HallOfFameFragment) {

    private var selectedCategory: String? = null

    val textWatcher: TextWatcher = object: TextWatcher {
        // TODO: make sure the recycler view is removed when nothing is there
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            searchCategory(s!!, fragment.categoryList, fragment.categorySearchAdapter!!)

        }

    }

    fun onCategorySelected(category: String) {

        fragment.chipGroup!!.removeAllViews()
        fragment.categoryList.clear()
        fragment.categorySearchAdapter!!.notifyDataSetChanged()
        selectedCategory = category
        fragment.byCategoryButton!!.isEnabled = false
        val chip = Chip(fragment.context)

        chip.text = category
        chip.isCloseIconVisible = true

        chip.setOnCloseIconClickListener {
            fragment.chipGroup!!.removeAllViews()
            selectedCategory = null
            fragment.byCategoryButton!!.isEnabled = true
        }

        fragment.chipGroup!!.addView(chip)

    }

    fun showFilterView(layout: FrameLayout) {
        layout.visibility = View.VISIBLE
    }

    fun removeView(layout: FrameLayout) {
        layout.visibility = View.GONE
    }

    fun submitFilter() {

        val locationState = fragment.locationButton!!.isChecked
        val orderCountByState = fragment.orderByCountButton!!.isChecked
        val descendingDirectionState = fragment.descendingDirectionButton!!.isChecked
        val byCategoryState = fragment.byCategoryButton!!.isChecked

        fragment.progressLayout!!.visibility = View.VISIBLE

        fragment.mainLayout!!.removeAllViews()



        when {
            byCategoryState && selectedCategory == null -> {

                CategoryDAO.getUsersByCategory(
                        orderCountByState,
                        descendingDirectionState,
                        locationState) { category, categoryList ->

                    fragment.progressLayout!!.visibility = View.GONE

                    val textView = TextView(ContextThemeWrapper(
                            fragment.context,
                            R.style.CategoryTextView)
                    )

                    textView.text = category.capitalize()

                    val rvContacts = RecyclerView(fragment.context!!)
                    rvContacts.setHasFixedSize(true)
                    val contactAdapter = CategoryRankingAdapter(
                            categoryList,
                            fragment,
                            R.layout.single_user_view_horizontal
                    )

                    rvContacts.adapter = contactAdapter
                    rvContacts.layoutManager = LinearLayoutManager(
                            fragment.activity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                    )

                    fragment.mainLayout!!.addView(textView)
                    fragment.mainLayout!!.addView(rvContacts)
                }
            }
            !byCategoryState && selectedCategory == null -> {
                UserDAO.getUsersBy(orderCountByState,
                        descendingDirectionState,
                        locationState) { userList ->

                    fragment.progressLayout!!.visibility = View.GONE

                    val textView = TextView(ContextThemeWrapper(
                            fragment.context,
                            R.style.CategoryTextView)
                    )
                    textView.text = "All users"

                    val rvContacts = RecyclerView(fragment.context!!)
                    rvContacts.setHasFixedSize(true)

                    val contactAdapter = UserRankingAdapter(
                            userList,
                            fragment
                    )
                    rvContacts.adapter = contactAdapter
                    rvContacts.layoutManager = LinearLayoutManager(fragment.activity)

                    fragment.mainLayout!!.addView(textView)
                    fragment.mainLayout!!.addView(rvContacts)

                }
            }
            selectedCategory != null -> {

                CategoryDAO.getUsersForCategory(selectedCategory!!,
                        orderCountByState,
                        descendingDirectionState,
                        locationState) { categoryList ->

                    fragment.progressLayout!!.visibility = View.GONE

                    val textView = TextView(ContextThemeWrapper(
                            fragment.context,
                            R.style.CategoryTextView)
                    )
                    textView.text = selectedCategory
                    val rvContacts = RecyclerView(fragment.context!!)
                    rvContacts.setHasFixedSize(true)
                    val contactAdapter = CategoryRankingAdapter(
                            categoryList,
                            fragment,
                            R.layout.single_user_view_vertical
                    )
                    rvContacts.adapter = contactAdapter
                    rvContacts.layoutManager = LinearLayoutManager(fragment.activity)

                    fragment.mainLayout!!.addView(textView)
                    fragment.mainLayout!!.addView(rvContacts)

                }
            }
        }

        removeView(fragment.filterLayout!!)
    }

    fun searchCategory(
            query: CharSequence,
            categoryList: ArrayList<String>,
            adapter: CategorySearchAdapter
    ) {
        categoryList.clear()
        if (query.isNotEmpty()) {
            CategoryDAO.queryCategory(query.toString()) {

                for (document in it) {
                    categoryList.add(document.id)
                }

                adapter.notifyDataSetChanged()

            }
        } else {
            adapter.notifyDataSetChanged()
        }
    }
}