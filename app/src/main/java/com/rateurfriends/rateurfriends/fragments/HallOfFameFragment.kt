package com.rateurfriends.rateurfriends.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.rateurfriends.rateurfriends.ContactProfileActivity

import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.CategorySearchAdapter
import com.rateurfriends.rateurfriends.adapters.ContactProfileAdapter
import com.rateurfriends.rateurfriends.adapters.CategoryRankingAdapter
import com.rateurfriends.rateurfriends.adapters.UserRankingAdapter
import com.rateurfriends.rateurfriends.controllers.HallOfFameController
import com.rateurfriends.rateurfriends.models.Category
import com.rateurfriends.rateurfriends.models.User

// TODO: enable view of the user profile

class HallOfFameFragment : Fragment(),
        CategoryRankingAdapter.ItemClickListener,
        UserRankingAdapter.ItemClickListener,
        CategorySearchAdapter.ItemClickListener {

    private var listener: OnFragmentInteractionListener? = null


    var categoryEditText: EditText? = null
    var cancelButton: Button? = null
    var submitButton: Button? = null

    var locationButton: SwitchCompat? = null
    var orderByCountButton: SwitchCompat? = null
    var descendingDirectionButton: SwitchCompat? = null
    var byCategoryButton: SwitchCompat? = null

    var categoryRecyclerView: RecyclerView? = null
    val categoryList: ArrayList<String> = arrayListOf()
    var categorySearchAdapter: CategorySearchAdapter? = null
    var chipGroup: ChipGroup? = null
    var filterLayout: FrameLayout? = null
    var mainLayout: LinearLayout? = null
    var progressLayout: FrameLayout? = null


    private var filterViewButton: MaterialButton? = null
    private var hallOfFameController: HallOfFameController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hallOfFameController = HallOfFameController(this)
        categorySearchAdapter = CategorySearchAdapter(categoryList, this)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (listener != null) {
            listener!!.onFragmentInteraction(this.getString(R.string.hall_of_fame_title));
        }

        val view = inflater.inflate(R.layout.fragment_hall_of_fame, container, false)
        mainLayout = view.findViewById(R.id.main_layout) as LinearLayout


        categoryEditText = view.findViewById(R.id.et_category_search) as EditText
        categoryRecyclerView = view.findViewById(R.id.rv_categories) as RecyclerView
        cancelButton = view.findViewById(R.id.bt_cancel) as Button
        submitButton = view.findViewById(R.id.bt_submit) as Button
        locationButton = view.findViewById(R.id.bt_local_filter) as SwitchCompat
        orderByCountButton = view.findViewById(R.id.bt_average_filter) as SwitchCompat
        descendingDirectionButton = view.findViewById(R.id.bt_high_to_low_filter) as SwitchCompat
        byCategoryButton = view.findViewById(R.id.bt_by_category) as SwitchCompat
        chipGroup = view.findViewById(R.id.chip_group) as ChipGroup
        filterLayout = view.findViewById(R.id.layout_filter) as FrameLayout
        filterViewButton = view.findViewById(R.id.bt_filter_view) as MaterialButton
        progressLayout = view.findViewById(R.id.progress_layout)

        filterViewButton!!.setOnClickListener{
            hallOfFameController!!.showFilterView(filterLayout!!)
        }

        cancelButton!!.setOnClickListener{
            hallOfFameController!!.removeView(filterLayout!!)
        }

        submitButton!!.setOnClickListener {
            hallOfFameController!!.submitFilter()
        }

        categoryRecyclerView!!.adapter = categorySearchAdapter
        categoryRecyclerView!!.layoutManager = LinearLayoutManager(this.activity)

        categoryEditText!!.addTextChangedListener(hallOfFameController!!.textWatcher)

        hallOfFameController!!.submitFilter()

        return view
    }

    override fun onItemClicked(user: User, phoneName: String) {

        val intent = Intent(activity, ContactProfileActivity::class.java)
        intent.putExtra("contact", user)
        intent.putExtra("phoneName", phoneName)
        startActivity(intent)

    }

    override fun onItemClicked(categoryName: String) {
        hallOfFameController!!.onCategorySelected(categoryName)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(title: String) {
        listener?.onFragmentInteraction(title)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HallOfFameFragment.
         */
        @JvmStatic
        fun newInstance() = HallOfFameFragment()
    }
}
