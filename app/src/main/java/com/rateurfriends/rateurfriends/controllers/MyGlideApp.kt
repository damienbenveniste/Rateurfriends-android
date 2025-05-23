package com.rateurfriends.rateurfriends.controllers

import android.content.Context
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
//import sun.text.normalizer.UTF16.append
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import java.io.InputStream


@GlideModule
class MyGlideApp : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference::class.java, InputStream::class.java,
                FirebaseImageLoader.Factory())
    }
}