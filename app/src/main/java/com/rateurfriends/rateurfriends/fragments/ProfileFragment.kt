package com.rateurfriends.rateurfriends.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.CategorySearchAdapter
import com.rateurfriends.rateurfriends.adapters.ProfileCategoryAdapter
import com.rateurfriends.rateurfriends.controllers.ProfileController
import com.rateurfriends.rateurfriends.customViews.IntegerButton
import com.rateurfriends.rateurfriends.customViews.LevelView
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.button.MaterialButton
import com.rateurfriends.rateurfriends.helperClasses.Globals
import kotlin.collections.ArrayList


class ProfileFragment : Fragment(),
        ProfileCategoryAdapter.ItemClickListener,
        CategorySearchAdapter.ItemClickListener {

    private var listener: OnFragmentInteractionListener? = null

    var profileImageView: ImageView? = null
    var levelView: LevelView? = null
    var levelTextView: TextView? = null
    var userNameTextView: TextView? = null
    var startNumberTextView: TextView? = null
    var meanStarTextView: TextView? = null
    var ratingBar: RatingBar? = null
    var spareStarsTextView: TextView? = null
    var spareCategoriesTextView: TextView? = null

    var photoMenuButton: MaterialButton? = null
    var rvCategories: RecyclerView? = null

    var addCategoryButton: MaterialButton? = null
    var addCategoryLayout: LinearLayout? = null

    var addCategoryEditText: EditText? = null
    var submitCategoryButton: Button? = null

    var takePictureButton: MaterialButton? = null
    var uploadPictureButton: MaterialButton? = null
    var editPictureLayout: FrameLayout? = null

    private var categoryRecyclerView: RecyclerView? = null

    val categoryNameList: ArrayList<String> = arrayListOf()
    var categorySearchAdapter: CategorySearchAdapter? = null


    private var profileController: ProfileController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileController = ProfileController(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        if (listener != null) {
            listener!!.onFragmentInteraction("Profile")
        }

        profileImageView = view.findViewById(R.id.iv_profile_picture) as ImageView
        levelView = view.findViewById(R.id.level_view) as LevelView
        levelTextView = view.findViewById(R.id.tv_level) as TextView
        userNameTextView = view.findViewById(R.id.tv_profile_name) as TextView
        startNumberTextView = view.findViewById(R.id.tv_start_number) as TextView
        meanStarTextView = view.findViewById(R.id.tv_mean_star) as TextView
        ratingBar = view.findViewById(R.id.star_rating) as RatingBar
        spareCategoriesTextView = view.findViewById(R.id.tv_spare_categories) as TextView
        spareStarsTextView = view.findViewById(R.id.tv_spare_stars) as TextView

        profileController!!.setUpProfile()

        editPictureLayout = view.findViewById(R.id.layout_edit_picture) as FrameLayout
        photoMenuButton = view.findViewById(R.id.bt_photo_menu) as MaterialButton
        rvCategories = view.findViewById(R.id.rvCategories) as RecyclerView
        takePictureButton = view.findViewById(R.id.bt_take_picture) as MaterialButton
        uploadPictureButton = view.findViewById(R.id.bt_upload_picture) as MaterialButton
        addCategoryButton = view.findViewById(R.id.bt_add_category) as MaterialButton
        addCategoryLayout = view.findViewById(R.id.ll_add_category) as LinearLayout
        addCategoryEditText = view.findViewById(R.id.ed_category_name) as EditText
        submitCategoryButton = view.findViewById(R.id.bt_submit_category) as Button
        categoryRecyclerView = view.findViewById(R.id.rv_category_search) as RecyclerView

        photoMenuButton?.setOnClickListener {
            profileController!!.showPictureEditView(editPictureLayout!!)
        }

        categoryRecyclerView!!.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        rvCategories!!.addItemDecoration(
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))

        editPictureLayout!!.setOnClickListener{
            profileController!!.removeView(editPictureLayout!!)
        }

        takePictureButton!!.setOnClickListener {
            profileController!!.checkPermissionForCameraView()
        }
        uploadPictureButton!!.setOnClickListener {
            profileController!!.checkPermissionForPictureUpload()
        }

        addCategoryButton!!.setOnClickListener {
            profileController!!.getAddCategoryForm()
        }

        if (Globals.getInstance().user!!.spareCategories <= 0) {
            submitCategoryButton!!.isEnabled = false
        }

        submitCategoryButton!!.setOnClickListener {
            profileController!!.submitCategory()
        }

        categorySearchAdapter = CategorySearchAdapter(categoryNameList, this)
        categoryRecyclerView!!.adapter = categorySearchAdapter
        categoryRecyclerView!!.layoutManager = LinearLayoutManager(this.activity)

        addCategoryEditText!!.addTextChangedListener(profileController!!.getTextChangedListener())

        profileController!!.getCategories(rvCategories!!)

        return view
    }

    override fun onItemClicked(categoryName: String) {
        profileController!!.onCategorySelected(categoryName)
    }

    override fun onItemClicked(layout: FrameLayout, integerButton: IntegerButton) {
        profileController!!.onCategoryClicked(layout, integerButton)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {

        profileController!!.onActivityResult(requestCode, resultCode, data, profileImageView!!)
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        profileController!!.handlePermission(requestCode, permissions, grantResults)
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(title: String)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
