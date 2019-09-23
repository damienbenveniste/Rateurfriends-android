package com.rateurfriends.rateurfriends.database.dao

import com.google.firebase.firestore.FirebaseFirestore
import com.rateurfriends.rateurfriends.models.Product

class PurchaseDAO() {

    companion object {

        private var instance: PurchaseDAO? = null

        @Synchronized
        fun getInstance(): PurchaseDAO {
            if (instance == null) {
                instance = PurchaseDAO()
            }
            return instance!!
        }

        fun capturePurchase(product: Product, onFailure: () -> Unit) {

            val purchaseId = product.productId + "_" + product.timeStamp.toString()

            if (product.userId.isEmpty() || purchaseId.isEmpty()) {
                onFailure()
                return
            }

            val db = FirebaseFirestore.getInstance()

            db.collection("UserAttribute")
                    .document(product.userId)
                    .collection("Purchase")
                    .document(purchaseId)
                    .set(product)
                    .addOnFailureListener {
                        onFailure()
                    }
        }

    }
}