package com.rateurfriends.rateurfriends.adapters

import android.content.Context
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.VoteDAO
import com.rateurfriends.rateurfriends.models.Category
import kotlin.math.roundToInt

class ContactCategoryAdapter constructor(
        private val userId: String,
        private val categoryList: ArrayList<Category>,
        private val listener: ItemClickListener,
        private val progressLayout: FrameLayout,
        private val mContext: Context) : RecyclerView.Adapter<ContactCategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.single_contact_category_view, null)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.categoryNameTextView.text = category.categoryName.capitalize()
        holder.ratingTextView.text = mContext
                .getString(R.string.mean_star_format)
                .format(category.meanStarNumber)

        holder.startRatingView.rating = category.meanStarNumber
        holder.starNumberTextView.text =  mContext
                .getString(R.string.star_number_format)
                .format(category.starNumber)

        holder.cancelButton.setOnClickListener { removeLayout(holder.ratingLayout) }
        holder.submitButton.setOnClickListener {
            submitRating(holder.ratingLayout, holder.ratingBar.rating, category, position)
        }

        VoteDAO.getVote(userId, category.userId, category.categoryName,
                onSuccess = {
                    holder.userVoteTextView.text = mContext
                            .getString(R.string.contact_profile_your_vote)
                            .format(it.getRatingStars())
                    holder.ratingBar.rating = it.rating.toFloat()
                },
                onFailure = {
                    println("Could not get the vote")
                }
        )
    }

    private fun removeLayout(layout: FrameLayout) {
        layout.visibility = View.GONE
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
                            mContext,
                            mContext.getString(R.string.contact_profile_could_not_update_category),
                            Toast.LENGTH_SHORT
                    ).show()
                })
    }

    private fun submitRating(layout: FrameLayout, rating: Float, category: Category, position: Int ) {

        val ratingInt = rating.roundToInt()

        progressLayout.visibility = View.VISIBLE
        VoteDAO.updateVoteForUser(
                ratingInt,
                userId,
                category.userId,
                category.categoryName,
                onSuccess = {
                    updateCategory(category, position)
                    progressLayout.visibility = View.GONE
                },
                onFailure = {
                    Toast.makeText(
                            mContext,
                            mContext.getString(R.string.contact_profile_could_not_update_vote),
                            Toast.LENGTH_SHORT
                    ).show()
                    progressLayout.visibility = View.GONE
                }
        )
        removeLayout(layout)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var categoryNameTextView: TextView
        internal var ratingTextView: TextView
        internal var starNumberTextView: TextView
        internal var startRatingView: RatingBar
        internal var ratingLayout: FrameLayout
        internal var cancelButton: AppCompatButton
        internal var submitButton: AppCompatButton
        internal var ratingBar: RatingBar
        internal var userVoteTextView: TextView

        init {
            categoryNameTextView = itemView.findViewById(R.id.tv_category_name) as TextView
            ratingTextView = itemView.findViewById(R.id.tv_rating) as TextView
            startRatingView = itemView.findViewById(R.id.star_rating) as RatingBar
            starNumberTextView = itemView.findViewById(R.id.tv_vote_number) as TextView
            ratingLayout = itemView.findViewById(R.id.layout_rating) as FrameLayout
            cancelButton = itemView.findViewById(R.id.bt_cancel) as AppCompatButton
            submitButton = itemView.findViewById(R.id.bt_submit) as AppCompatButton
            ratingBar = itemView.findViewById(R.id.rating_bar) as RatingBar
            userVoteTextView = itemView.findViewById(R.id.tv_user_vote) as TextView

            itemView.setOnClickListener {
                listener.onItemClicked(categoryList[adapterPosition], adapterPosition, ratingLayout)
            }

        }

    }

    interface ItemClickListener {
        fun onItemClicked(category: Category, position: Int, layout: FrameLayout)
    }

}