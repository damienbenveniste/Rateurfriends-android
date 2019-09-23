package com.rateurfriends.rateurfriends.fragments

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
//import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.ToggleButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.ContactProfileActivity

import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.ContactProfileAdapter
import com.rateurfriends.rateurfriends.adapters.FeedAdapter
import com.rateurfriends.rateurfriends.controllers.FeedController
import com.rateurfriends.rateurfriends.database.dao.FeedDAO
import com.rateurfriends.rateurfriends.models.Feed
import com.rateurfriends.rateurfriends.models.User


class FeedFragment : Fragment(),
        FeedAdapter.ItemClickListener {

    private var listener: OnFragmentInteractionListener? = null

    private var rvFeed: RecyclerView? = null
    var feedList: ArrayList<Feed> = arrayListOf()
    var feedAdapter: FeedAdapter? = null
    var feedButton: SwitchCompat? = null
    var progressLayout: FrameLayout? = null
    var emptyLayout: FrameLayout? = null

    private var feedController: FeedController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        feedAdapter = FeedAdapter(feedList, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (listener != null) {
            listener!!.onFragmentInteraction(this.getString(R.string.feed_title));
        }

        feedController = FeedController(this)

        val view = inflater.inflate(R.layout.fragment_feed, container, false)

        feedButton = view.findViewById(R.id.bt_feed_toggle) as SwitchCompat
        rvFeed = view.findViewById(R.id.rv_feed) as RecyclerView
        progressLayout = view.findViewById(R.id.progress_layout)
        emptyLayout = view.findViewById(R.id.empty_layout)

        rvFeed!!.adapter = feedAdapter
        rvFeed!!.layoutManager = LinearLayoutManager(this.activity)

        feedButton!!.setOnClickListener {
            feedController!!.changeFeedState()
        }

        feedController!!.changeFeedState()

        return view
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

    override fun onItemClicked(user: User, phoneName: String) {

        val intent = Intent(activity, ContactProfileActivity::class.java)
        intent.putExtra("contact", user)
        intent.putExtra("phoneName", phoneName)
        startActivity(intent)

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
        fun newInstance() = FeedFragment()
    }
}
