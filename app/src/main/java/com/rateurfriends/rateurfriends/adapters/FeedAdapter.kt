package com.rateurfriends.rateurfriends.adapters

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.customViews.LevelView
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.models.Feed
import com.rateurfriends.rateurfriends.models.User
import java.util.logging.Level

class FeedAdapter(
        private val feedList: List<Feed>,
        private val fragment: Fragment
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(fragment.context!!).inflate(R.layout.single_feed_view, null)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val feed = feedList[position]

        holder.feedTypeTextView.text = if (feed.feedType in listOf("", "level_passed"))
            "Level Passed!" else ""

        holder.message.text = Html.fromHtml(fragment.context!!.getString(R.string.feed_text)
                .format(
                        feed.userName,
                        feed.levelNumber(),
                        feed.getNearestStarThreshold()
                )
        )

        holder.levelView.levelText = feed.level

        PictureDAO.populateImageViewWithUserId(
                feed.userId,
                holder.pictureImageView,
                fragment.context!!
        )


    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var pictureImageView: ImageView
        internal var message: TextView
        internal var levelView: LevelView
        internal var feedTypeTextView: TextView


        init {
            pictureImageView = itemView.findViewById(R.id.iv_picture_contact) as ImageView
            message = itemView.findViewById(R.id.tv_message) as TextView
            levelView = itemView.findViewById(R.id.level_view) as LevelView
            feedTypeTextView = itemView.findViewById(R.id.tv_feed_type) as TextView

            itemView.setOnClickListener {
                val feed = feedList[adapterPosition]
                val userId = feed.userId
                UserDAO.getUser(userId) {
                    val listener = fragment as ItemClickListener
                    listener.onItemClicked(it, "")
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return feedList.size
    }

    interface ItemClickListener {
        fun onItemClicked(user: User, phoneName: String)
    }


}