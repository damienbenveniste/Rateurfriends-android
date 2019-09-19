package com.rateurfriends.rateurfriends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.customViews.LevelView
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.models.Contact
import com.rateurfriends.rateurfriends.models.User

class ContactProfileAdapter constructor(
        private val contactList: ArrayList<Contact>,
        private val fragment: Fragment
) : RecyclerView.Adapter<ContactProfileAdapter.ContactViewHolder>(), Filterable {

    private val userMap: HashMap<String, User> = HashMap()
    private var contactSearchList: List<Contact> = contactList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater
                .from(fragment.context)
                .inflate(R.layout.single_contact_view, null)

        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactSearchList[position]
        val userId = contact.userId
        // todo: show phoneName!!
        if (userId in userMap) {
            val user = userMap[userId]!!
            holder.contactNameTextView.text = user.userName.capitalize()
            if (contact.phoneName.isNotEmpty()) {
                holder.phoneNameTextView.text = contact.phoneName.capitalize()

            }
            holder.startRatingView.rating = user.meanStarNumber
            holder.starNumberTextView.text = fragment
                    .getString(R.string.star_number_format)
                    .format(user.totalStarNumber)

            holder.starMeanTextView.text = fragment
                    .getString(R.string.mean_star_format)
                    .format(user.meanStarNumber)

            holder.levelView.levelText = user.level

        } else {

            holder.contactNameTextView.text = ""
            holder.startRatingView.rating = 0.0f
            holder.starNumberTextView.text = ""
            holder.starMeanTextView.text = ""
            holder.levelView.levelText = ""

            UserDAO.getUser(userId) {
                holder.contactNameTextView.text = it.userName.capitalize()
                if (contact.phoneName.isNotEmpty()) {
                    holder.phoneNameTextView.text = contact.phoneName.capitalize()

                }
                holder.startRatingView.rating = it.meanStarNumber
                holder.starNumberTextView.text =  fragment
                        .getString(R.string.star_number_format)
                        .format(it.totalStarNumber)

                holder.starMeanTextView.text = fragment
                        .getString(R.string.mean_star_format)
                        .format(it.meanStarNumber)

                holder.levelView.levelText = it.level
                userMap[it.userId] = it
            }
        }

        PictureDAO.populateImageViewWithUserId(
                userId,
                holder.profilePictureImageView,
                fragment.context!!
        )
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val contactNameTextView: TextView  = itemView.findViewById(R.id.tv_contact_name)
        internal var profilePictureImageView: ImageView = itemView.findViewById(
                R.id.iv_picture_contact)
        internal var startRatingView: RatingBar = itemView.findViewById(R.id.star_rating)
        internal var starNumberTextView: TextView = itemView.findViewById(R.id.tv_star_count)
        internal var starMeanTextView: TextView = itemView.findViewById(R.id.tv_star_mean)
        internal var levelView: LevelView = itemView.findViewById(R.id.level_view)
        internal var phoneNameTextView: TextView = itemView.findViewById(R.id.tv_phone_name)

        init {

            itemView.setOnClickListener {
                val contact = contactSearchList[adapterPosition]
                val userId = contact.userId
                if (userId in userMap) {
                    val listener = fragment as ItemClickListener
                    listener.onItemClicked(userMap[userId]!!, contact.phoneName)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return contactSearchList.size
    }

    override fun getFilter(): Filter {
        contactList.forEach {
            if (it.userId !in userMap) {
                UserDAO.getUser(it.userId) { user ->
                    userMap[user.userId] = user
                }
            }
        }
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    contactSearchList = contactList
                } else {
                    val filteredList = ArrayList<Contact>()
                    for (contact in contactList) {
                        if (contact.userId in userMap) {
                            val user = userMap[contact.userId]!!
                            if (user.userName.toLowerCase().contains(charString.toLowerCase()) ||
                                    user.phoneNumber.contains(charSequence) ||
                                    contact.phoneName.toLowerCase().contains(
                                            charString.toLowerCase())) {
                                filteredList.add(contact)
                            }
                        }
                    }
                    contactSearchList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = contactSearchList
                return filterResults
            }
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                contactSearchList = filterResults.values as ArrayList<Contact>
                notifyDataSetChanged()
            }
        }
    }

    interface ItemClickListener {
        fun onItemClicked(user: User, phoneName: String)
    }

}