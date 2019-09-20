package com.rateurfriends.rateurfriends.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User
import io.opencensus.resource.Resource
import org.xmlpull.v1.XmlPullParser

class CategoryRankingAdapter constructor(
        private val categoryList: List<Category>,
        private val fragment: Fragment,
        private val resourceLayout: Int
) : RecyclerView.Adapter<CategoryRankingAdapter.ContactViewHolder>() {
    private val userMap: HashMap<String, User> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(fragment.context!!).inflate(resourceLayout, null)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val category = categoryList[position]
        val userId = category.userId

        if (userId in userMap) {
            val user = userMap[userId]!!
            holder.contactNameTextView.text = user.userName.capitalize()

        } else {

            holder.contactNameTextView.text = ""

            UserDAO.getUser(userId,
                    onSuccess = {
                        holder.contactNameTextView.text = it.userName.capitalize()
                        userMap[it.userId] = it
                    },
                    onFailure = {
                        println("Could not get user")
                    }
            )
        }

        holder.categoryNameTextView.text = category.categoryName.capitalize()
        holder.startRatingView.rating = category.meanStarNumber
        holder.starNumberTextView.text = fragment
                .getString(R.string.star_number_format)
                .format(category.starNumber)

        holder.starMeanTextView.text = fragment
                .getString(R.string.mean_star_format)
                .format(category.meanStarNumber)

        PictureDAO.populateImageViewWithUserId(
                category.userId,
                holder.profilePictureImageView,
                fragment.context!!
        )
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var contactNameTextView: TextView
        internal var profilePictureImageView: ImageView
        internal var startRatingView: RatingBar
        internal var starNumberTextView: TextView
        internal var starMeanTextView: TextView
        internal var categoryNameTextView: TextView

        init {
            contactNameTextView = itemView.findViewById(R.id.tv_contact_name) as TextView
            profilePictureImageView = itemView.findViewById(R.id.iv_picture_contact) as ImageView
            startRatingView = itemView.findViewById(R.id.star_rating) as RatingBar
            starNumberTextView = itemView.findViewById(R.id.tv_star_count) as TextView
            starMeanTextView = itemView.findViewById(R.id.tv_star_mean) as TextView
            categoryNameTextView = itemView.findViewById(R.id.tv_category_name) as TextView

            itemView.setOnClickListener {
                val category = categoryList[adapterPosition]
                val userId = category.userId
                if (userId in userMap) {
                    val listener = fragment as ItemClickListener
                    listener.onItemClicked(userMap[userId]!!, "")
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }


    interface ItemClickListener {
        fun onItemClicked(user: User, phoneName: String)
    }

}