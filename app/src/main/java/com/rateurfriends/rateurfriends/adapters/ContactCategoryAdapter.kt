package com.rateurfriends.rateurfriends.adapters

import android.content.Context
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.customViews.RatingView
import com.rateurfriends.rateurfriends.database.dao.CategoryDAO
import com.rateurfriends.rateurfriends.database.dao.VoteDAO
import com.rateurfriends.rateurfriends.models.Category

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
        holder.categoryNameTextView.text = category.categoryName
        holder.ratingTextView.text = "%.1f".format(category.meanStarNumber)
        holder.startRatingView.rating = category.meanStarNumber.toFloat()
        holder.starNumberTextView.text = "★%d".format(category.starNumber)

        holder.cancelButton.setOnClickListener { removeLayout(holder.ratingLayout) }
        holder.submitButton.setOnClickListener {
            submitRating(holder.ratingLayout, holder.ratingBar.rating, category, position)
        }

        VoteDAO.getVote(userId, category.userId, category.categoryName) {
            holder.userVoteTextView.text = "Your vote: %s".format(it.getRatingStars())
        }
    }

    private fun removeLayout(layout: FrameLayout) {
        layout.visibility = View.GONE
    }

    private fun updateCategory(category: Category, position: Int) {
        CategoryDAO.getCategoryForUser(category.userId, category.categoryName) {
            document ->
            if (document.exists()) {
                val category = document.toObject(Category::class.java)
                categoryList.set(position, category!!)
                this.notifyItemChanged(position)
            }
        }
    }

    private fun submitRating(layout: FrameLayout, rating: Float, category: Category, position: Int ) {

        progressLayout.visibility = View.VISIBLE
        VoteDAO.updateVoteForUser(
                rating.toInt(),
                userId,
                category.userId,
                category.categoryName
        ) {
            updateCategory(category, position)
            progressLayout.visibility = View.GONE
        }
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