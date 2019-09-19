package com.rateurfriends.rateurfriends.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.rateurfriends.rateurfriends.R

class ProductsAdapter(
        private val productList: List<SkuDetails>,
        private val onProductClicked: (SkuDetails) -> Unit
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    override fun getItemCount(): Int = productList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsAdapter.ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.single_product_view, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val title = productList[position].title.split(" (")
        holder.productNameTextView.text = title[0].capitalize()
        holder.descriptionTextView.text = productList[position].description.replace(
                "\\s".toRegex(),
                " ")
        holder.buyButton.text = productList[position].price

        holder.buyButton.setOnClickListener { onProductClicked(productList[position]) }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var productNameTextView: TextView
        internal var descriptionTextView: TextView
        internal var buyButton: Button

        init {
            productNameTextView = itemView.findViewById(R.id.tv_product_name) as TextView
            descriptionTextView = itemView.findViewById(R.id.tv_product_description) as TextView
            buyButton = itemView.findViewById(R.id.bt_buy) as Button

        }

    }
}