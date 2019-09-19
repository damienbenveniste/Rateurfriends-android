package com.rateurfriends.rateurfriends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.customViews.LevelView
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.models.User

class UserRankingAdapter constructor(
        private val userList: List<User>,
        private val fragment: Fragment
) : RecyclerView.Adapter<UserRankingAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater
                .from(fragment.context)
                .inflate(R.layout.single_contact_view, null)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        // todo: show phoneName!!

        holder.contactNameTextView.text = user.userName.capitalize()
        holder.startRatingView.rating = user.meanStarNumber

        holder.starNumberTextView.text = fragment
                .getString(R.string.star_number_format)
                .format(user.totalStarNumber)

        holder.starMeanTextView.text = fragment
                .getString(R.string.mean_star_format)
                .format(user.meanStarNumber)

        holder.levelView.levelText = user.level

        PictureDAO.populateImageViewWithUserId(
                user.userId,
                holder.profilePictureImageView,
                fragment.context!!
        )

    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val contactNameTextView: TextView  = itemView.findViewById(R.id.tv_contact_name)
        internal var profilePictureImageView: ImageView = itemView.findViewById(
                R.id.iv_picture_contact)
        internal var startRatingView: RatingBar = itemView.findViewById(R.id.star_rating)
        internal var starNumberTextView: TextView = itemView.findViewById(R.id.tv_star_count)
        internal var starMeanTextView: TextView = itemView.findViewById(R.id.tv_star_mean)
        internal var levelView: LevelView = itemView.findViewById(R.id.level_view)

        init {

            itemView.setOnClickListener {
                val user = userList[adapterPosition]
                val listener = fragment as ItemClickListener
                listener.onItemClicked(user, "")
            }

        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    interface ItemClickListener {
        fun onItemClicked(user: User, phoneName: String)
    }

}