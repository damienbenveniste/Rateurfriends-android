package com.rateurfriends.rateurfriends

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.adapters.ContactCategoryAdapter
import com.rateurfriends.rateurfriends.controllers.ContactProfileController
import com.rateurfriends.rateurfriends.customViews.LevelView
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User


class ContactProfileActivity : AppCompatActivity() , ContactCategoryAdapter.ItemClickListener {

    var contactImageView: ImageView? = null
    var rvCategories: RecyclerView? = null
    var nameTextView: TextView? = null
    var contactProfileController = ContactProfileController(this)
    var rootView: ConstraintLayout? = null
    var user: User? = null
    var phoneName: String? = null
    var phoneNameTextView: TextView? = null
    var levelView: LevelView? = null
    var levelTextView: TextView? = null
    var ratingBar: RatingBar? = null
    var meanStarTextView: TextView? = null
    var startNumberTextView: TextView? = null
    var progressLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_profile)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        rootView = findViewById(R.id.constraint_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        user = intent.getParcelableExtra("contact")
        phoneName = intent.getStringExtra("phoneName")

        nameTextView = findViewById(R.id.tv_profile_name)
        phoneNameTextView = findViewById(R.id.tv_phone_name)
        rvCategories = findViewById(R.id.rv_categories)
        contactImageView = findViewById(R.id.iv_contact_picture)
        levelView = findViewById(R.id.level_view)
        levelTextView = findViewById(R.id.tv_level)
        ratingBar = findViewById(R.id.star_rating)
        meanStarTextView = findViewById(R.id.tv_mean_star)
        startNumberTextView = findViewById(R.id.tv_start_number)
        progressLayout = findViewById(R.id.progress_layout)

        rvCategories!!.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        contactProfileController.setUpProfile()
        contactProfileController.getCategories(user!!, rvCategories!!)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onItemClicked(category: Category, position: Int, layout: FrameLayout) {

        if (layout.visibility == View.GONE) {
            layout.visibility = View.VISIBLE
        }
    }

}
