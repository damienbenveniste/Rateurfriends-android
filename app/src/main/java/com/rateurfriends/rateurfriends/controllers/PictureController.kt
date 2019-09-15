package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.auth.UserProfileChangeRequest
import android.content.Context
//import android.support.v4.app.NotificationCompat.getExtras
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
//import android.media.ExifInterface
import androidx.exifinterface.media.ExifInterface
//import android.support.v4.app.Fragment
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.helperClasses.FileChooser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class PictureController(
        val fragment: Fragment
) {

    private val TAKE_PHOTO_REQUEST = 34
    private val SELECT_AN_IMAGE = 77

    fun getCameraView() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        fragment.startActivityForResult(intent, TAKE_PHOTO_REQUEST)
    }

    fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri): Bitmap {
        val MAX_HEIGHT = 1024
        val MAX_WIDTH = 1024

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true;
        var imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        var img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    private fun getInputStream(bitmap: Bitmap): ByteArrayInputStream {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        val bitmapdata = bos.toByteArray()
        return ByteArrayInputStream(bitmapdata)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options,
                                      reqWidth: Int,
                                      reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            val totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private fun rotateImageIfRequired(img: Bitmap, selectedImage: Uri): Bitmap {

        val ei = ExifInterface(FileChooser.getPath(fragment.context!!,selectedImage)!!)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true)
        img.recycle()
        return rotatedImg
    }

    fun processCapturedPhoto(requestCode: Int, resultCode: Int, data: Intent?, profileImageView: ImageView) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                TAKE_PHOTO_REQUEST -> {
                    val photo = data?.extras?.get("data")
                    if (photo != null) {
                        val rotatedPhoto = rotateImage(photo as Bitmap, 270f)

                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        savePicture(userId, rotatedPhoto) { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                populateImageView(profileImageView)
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setPhotoUri(downloadUri)
                                        .build()
                                FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdates)
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    }
                }
                SELECT_AN_IMAGE -> {
                    val uri = data?.data
                    if (uri != null) {
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        val img = handleSamplingAndRotationBitmap(fragment.context!!, uri)
                        savePicture(userId, img) { task ->
                            if (task.isSuccessful) {
                                val downloadUri = task.result
                                populateImageViewFromFile(profileImageView, uri, fragment.context!!)
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                        .setPhotoUri(downloadUri)
                                        .build()
                                FirebaseAuth.getInstance().currentUser!!.updateProfile(profileUpdates)
                            } else {
                                // Handle failures
                                // ...
                            }
                        }
                    }
                }
            }
        }
    }

    private fun populateImageViewFromFile(imageView: ImageView, filePath: Uri, context: Context) {
        GlideApp.with(context)
                .load(filePath)
                .override(600, 400)
                .into(imageView)
    }

    fun getImageChooserView() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        fragment.startActivityForResult(Intent.createChooser(intent, "Select an image"), SELECT_AN_IMAGE);
    }

    private fun savePicture(userId: String, bitmap: Bitmap, callback: (Task<Uri>) -> Unit) {
        PictureDAO.saveProfilePictureFromBitmap(userId, bitmap, callback)
    }

    private fun savePicture(userId: String, filePath: Uri, callback: (Task<Uri>) -> Unit) {
        PictureDAO.saveProfilePictureFromFile(userId, filePath, callback)
    }

    fun populateImageView(imageView: ImageView) {
        if (FirebaseAuth.getInstance().currentUser?.photoUrl != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            PictureDAO.populateImageView(userId!!, imageView, fragment.context!!)
        }
    }

    fun populateImageViewForUser(userId: String, imageView: ImageView) {
        PictureDAO.populateImageView(userId, imageView, fragment.context!!)
    }

}