package com.rateurfriends.rateurfriends.controllers

import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.ProductsAdapter
import com.rateurfriends.rateurfriends.database.dao.PurchaseDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.fragments.MarketPlaceFragment
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Product

class MarketPlaceController(var fragment: MarketPlaceFragment) {

    private lateinit var billingClient: BillingClient
    private lateinit var productsAdapter: ProductsAdapter
    private var skuMap: Map<String, SkuDetails>? = null

    fun setupBillingClient() {
        billingClient = BillingClient
                .newBuilder(fragment.context!!)
                .enablePendingPurchases()
                .setListener(fragment)
                .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadProducts()
                    println("BILLING | startConnection | RESULT OK")
                } else {
                    println("BILLING | startConnection | RESULT: $billingResult!!.responseCode")
                }
            }

            override fun onBillingServiceDisconnected() {
                println("BILLING | onBillingServiceDisconnected | DISCONNECTED")
            }
        })
    }

    fun loadProducts() {

        if (billingClient.isReady) {
            val params = SkuDetailsParams
                    .newBuilder()
                    .setSkusList(Product.skuList)
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                if (responseCode.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("querySkuDetailsAsync, responseCode: ${responseCode.responseCode}")
                    initProductAdapter(skuDetailsList)
                } else {
                    println("Can't querySkuDetailsAsync, responseCode: ${responseCode.responseCode}")
                }
            }
        } else {
            println("Billing Client not ready")
        }
    }

    fun setupTextViews() {
        val user = Globals.getInstance().user

        if (user != null) {
            fragment.spareStarTextView!!.text = fragment
                    .getString(R.string.market_place_spare_stars_text_view)
                    .format(user.spareStars)

            fragment.spareCategoryTextView!!.text = fragment
                    .getString(R.string.market_place_spare_categories_text_view)
                    .format(user.spareCategories)
        }

    }

    private fun initProductAdapter(skuDetailsList: List<SkuDetails>) {

        val reorderedList = reorderList(skuDetailsList)

        productsAdapter = ProductsAdapter(reorderedList) {
            val billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setSkuDetails(it)
                    .build()
            billingClient.launchBillingFlow(fragment.activity, billingFlowParams)
        }

        fragment.productRecyclerView!!.adapter = productsAdapter
        fragment.productRecyclerView!!.layoutManager = LinearLayoutManager(fragment.activity)

        fragment.progressLayout!!.visibility = View.GONE
    }

    private fun reorderList(list: List<SkuDetails>): List<SkuDetails> {
        skuMap = list.map{ it.sku to it }.toMap()
        return Product.skuList.map{ skuMap!!.getValue(it) }
    }

    fun onPurchasesUpdated(billingResult: BillingResult?,
                           purchases: MutableList<Purchase>?) {

        val purchase = purchases?.first()

        if (billingResult!!.responseCode == BillingClient.BillingResponseCode.OK &&
                purchase?.purchaseToken != null) {

            fragment.progressLayout!!.visibility = View.VISIBLE

            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val sku = purchase.sku
            if (sku in Product.spareStarsMap.keys) {
                UserDAO.incrementSpareStarsForUser(
                        userId,
                        Product.spareStarsMap.getValue(sku),
                        onSuccess = {
                            if (Globals.getInstance().user != null) {
                                Globals.getInstance().user!!.spareStars +=
                                        Product.spareStarsMap.getValue(sku)

                                fragment.spareStarTextView!!.text = fragment
                                        .getString(R.string.market_place_spare_stars_text_view)
                                        .format(Globals.getInstance().user!!.spareStars)
                            }
                            fragment.progressLayout!!.visibility = View.GONE
                        },
                        onFailure = {
                            fragment.progressLayout!!.visibility = View.GONE
                            Toast.makeText(
                                    fragment.context,
                                    fragment.getString(R.string.market_place_could_not_update_stars),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                )

            }
            else if (sku in Product.spareCategoriesMap.keys) {
                UserDAO.incrementSpareCategoriesForUser(
                        userId,
                        Product.spareCategoriesMap.getValue(sku),
                        onSuccess = {
                            if (Globals.getInstance().user != null) {
                                Globals.getInstance().user!!.spareCategories +=
                                        Product.spareCategoriesMap.getValue(sku)
                                fragment.spareCategoryTextView!!.text = fragment
                                        .getString(R.string.market_place_spare_categories_text_view)
                                        .format(Globals.getInstance().user!!.spareCategories)

                            }
                            fragment.progressLayout!!.visibility = View.GONE
                        },
                        onFailure = {
                            fragment.progressLayout!!.visibility = View.GONE
                            Toast.makeText(
                                    fragment.context,
                                    fragment.getString(R.string.market_place_could_not_update_categories),
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                )
            }
            val product = Product(
                    userId = userId,
                    productId = sku,
                    price=if (skuMap != null) skuMap!!.getValue(sku).price else ""
            )
            PurchaseDAO.capturePurchase(product,
                    onFailure = {
                        println("Could not capture the payment")
                    }
            )
            allowMultiplePurchases(purchases)
        }
    }

    private fun allowMultiplePurchases(purchases: MutableList<Purchase>?) {
        val purchase = purchases?.first()
        if (purchase != null) {

            val consumeParams =
                    ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .setDeveloperPayload(purchase.developerPayload)
                            .build()

            billingClient.consumeAsync(consumeParams) { responseCode, purchaseToken ->
                if (responseCode.responseCode == BillingClient.BillingResponseCode.OK &&
                        purchaseToken != null) {
                    println("AllowMultiplePurchases success, responseCode: ${responseCode.responseCode}")
                } else {
                    println("Can't allowMultiplePurchases, responseCode: ${responseCode.responseCode}")
                }
            }
        }
    }

}