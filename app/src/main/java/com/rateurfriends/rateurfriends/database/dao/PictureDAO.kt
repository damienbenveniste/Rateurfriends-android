package com.rateurfriends.rateurfriends.database.dao


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.rateurfriends.rateurfriends.controllers.GlideApp
import java.io.ByteArrayOutputStream

class PictureDAO {

    companion object {

        private var instance: PictureDAO? = null

        @Synchronized
        fun getInstance(): PictureDAO {
            if (instance == null) {
                instance = PictureDAO()
            }
            return instance!!
        }

        fun saveProfilePictureFromFile(userId: String, filePath: Uri, callback: (Task<Uri>) -> Unit) {
            val ref = FirebaseStorage.getInstance().reference.child("profile_pictures/" + userId)
            ref.putFile(filePath).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    println("cannot upload")
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task -> callback(task) }
        }

        fun saveProfilePictureFromBitmap(userId: String, bitmap: Bitmap, callback: (Task<Uri>) -> Unit) {

            if (userId.isEmpty()) {
                return
            }

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val ref = FirebaseStorage.getInstance().reference.child("profile_pictures/" + userId)
            ref.putBytes(data).continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    println("cannot upload")
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task -> callback(task) }
        }

        fun populateImageView(userId: String, imageView: ImageView, context: Context) {

            if (userId.isEmpty()) {
                return
            }

            val gsReference = FirebaseStorage.getInstance().getReference("profile_pictures/" + userId)
            GlideApp
                    .with(context)
                    .load(gsReference)
                    .override(400, 400)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)

        }

        fun populateImageViewWithUserId(userId: String, imageView: ImageView, context: Context) {

            if (userId.isEmpty()) {
                return
            }

            val gsReference = FirebaseStorage.getInstance().getReference("profile_pictures/" + userId)
            GlideApp
                    .with(context)
                    .load(gsReference)
                    .override(200, 200)
                    .centerCrop()
//                .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(imageView)
        }

    }
}