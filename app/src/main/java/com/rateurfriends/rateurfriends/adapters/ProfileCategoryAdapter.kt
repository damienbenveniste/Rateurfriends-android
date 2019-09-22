package com.rateurfriends.rateurfriends.adapters

//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.customViews.IntegerButton
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.fragments.ProfileFragment
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Category



class ProfileCategoryAdapter constructor(
        private val userId: String,
        private val categoryList: ArrayList<Category>,
        private val fragment: ProfileFragment) : RecyclerView.Adapter<ProfileCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(fragment.context).inflate(R.layout.single_profile_category_view, null)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryNameTextView.text = category.categoryName.capitalize()

        holder.ratingTextView.text = fragment
                .getString(R.string.mean_star_format)
                .format(category.meanStarNumber)

        holder.publicCheckbox.isChecked = category.public
        holder.startRatingView.rating = category.meanStarNumber
        holder.starNumberTextView.text = fragment
                .getString(R.string.star_number_format)
                .format(category.starNumber)

        holder.cancelButton.setOnClickListener {
            removeLayout(holder.incrementLayout, holder.integerButton)
        }

        holder.publicCheckbox.setOnClickListener{
            category.changePublicVisibility(userId)
        }

        holder.submitButton.setOnClickListener {
            submitIncrement(holder.incrementLayout, holder.integerButton, category, position)
        }
    }

    private fun updateCategory(category: Category, position: Int) {
        CategoryDAO.getCategoryForUser(
                category.userId,
                category.categoryName,
                onSuccess = {
                    document ->
                    if (document.exists()) {
                        val newCategory = document.toObject(Category::class.java)
                        categoryList[position] = newCategory!!
                        this.notifyItemChanged(position)
                    }
                },
                onFailure = {
                    Toast.makeText(
                            fragment.context,
                            fragment.getString(R.string.profile_could_not_update_category),
                            Toast.LENGTH_SHORT
                    ).show()
                }

        )
    }

    private fun removeLayout(layout: FrameLayout, integerButton: IntegerButton, reset: Boolean=true) {
        if (reset) {
            integerButton.integerEditText.setText("0")
        }
        layout.visibility = View.GONE
    }

    private fun submitIncrement(layout: FrameLayout, integerButton: IntegerButton, category: Category, position: Int ) {

        fragment.progressLayout!!.visibility = View.VISIBLE
        try {
            if (Globals.getInstance().user!!.spareStars - integerButton.integerValue >= 0
                    && integerButton.integerValue > 0) {
                UserDAO.transferStarsForUser(
                        userId,
                        integerButton.integerValue,
                        category,
                        onSuccess = {
                            fragment.progressLayout!!.visibility = View.GONE
                            updateCategory(category, position)
                            Globals.getInstance().user!!.spareStars -= integerButton.integerValue

                            removeLayout(layout, integerButton, false)
                        } ,
                        onFailure = {

                            fragment.progressLayout!!.visibility = View.GONE
                            removeLayout(layout, integerButton, true)
                        }
                )
            } else {
                throw Exception("Globals.getInstance().user!!.spareStars - integerButton.integerValue < 0")
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var categoryNameTextView: TextView
        internal var ratingTextView: TextView
        internal var publicCheckbox: CheckBox
        internal var startRatingView: RatingBar
        internal var starNumberTextView: TextView
        internal var incrementLayout: FrameLayout
        internal var cancelButton: AppCompatButton
        internal var submitButton: AppCompatButton
        internal var integerButton: IntegerButton

        init {
            categoryNameTextView = itemView.findViewById(R.id.tv_category_name) as TextView
            ratingTextView = itemView.findViewById(R.id.tv_rating) as TextView
            publicCheckbox = itemView.findViewById(R.id.public_checkbox) as CheckBox
            startRatingView = itemView.findViewById(R.id.star_rating) as RatingBar
            starNumberTextView = itemView.findViewById(R.id.tv_vote_number) as TextView
            incrementLayout = itemView.findViewById(R.id.layout_increment_stars) as FrameLayout
            cancelButton = itemView.findViewById(R.id.bt_cancel) as AppCompatButton
            submitButton = itemView.findViewById(R.id.bt_submit) as AppCompatButton
            integerButton = itemView.findViewById(R.id.integer_button) as IntegerButton


            itemView.setOnClickListener {
                fragment.onItemClicked(incrementLayout, integerButton)
            }
        }

    }

    interface ItemClickListener {
        fun onItemClicked(layout: FrameLayout, integerButton: IntegerButton)
    }

}