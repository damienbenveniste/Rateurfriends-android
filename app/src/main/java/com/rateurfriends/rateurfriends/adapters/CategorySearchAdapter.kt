package com.rateurfriends.rateurfriends.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.fragments.ProfileFragment

class CategorySearchAdapter(
        private val categoryNameList: ArrayList<String>,
        private val fragment: Fragment
): RecyclerView.Adapter<CategorySearchAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater
                .from(fragment.context)
                .inflate(R.layout.single_category, null)

        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryNameList[position]
        holder.categoryNameTextView.text = category
    }

    override fun getItemCount(): Int {
        return categoryNameList.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var categoryNameTextView = itemView.findViewById(R.id.tv_category_name) as TextView

        init {
            val listener = fragment as ItemClickListener
            itemView.setOnClickListener {
                listener.onItemClicked(categoryNameList!![adapterPosition])
            }
        }
    }

    interface ItemClickListener {
        fun onItemClicked(category: String)
    }

}