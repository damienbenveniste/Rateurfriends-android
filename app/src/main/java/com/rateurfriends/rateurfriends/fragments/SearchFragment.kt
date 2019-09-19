package com.rateurfriends.rateurfriends.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import com.rateurfriends.rateurfriends.models.User

import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.ContactProfileAdapter
import android.view.MenuInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.ContactProfileActivity
import com.rateurfriends.rateurfriends.controllers.SearchController

class SearchFragment : Fragment() , ContactProfileAdapter.ItemClickListener {


    private var listener: OnFragmentInteractionListener? = null
    private var searchController: SearchController? = null

    var rvContacts: RecyclerView? = null
    var searchView: SearchView? = null
    var progressLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchController = SearchController(this)

        if (listener != null) {
            listener!!.onFragmentInteraction(this.getString(R.string.search_title));
        }

        rvContacts = view.findViewById(R.id.rv_contacts) as RecyclerView
        progressLayout = view.findViewById(R.id.progress_layout)

        searchController!!.getContacts()


        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_contact, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchController!!.setupSearch(searchItem)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_search) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(user: User, phoneName: String) {

        val intent = Intent(activity, ContactProfileActivity::class.java)
        intent.putExtra("contact", user)
        intent.putExtra("phoneName", phoneName)
        startActivity(intent)

    }

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
        // TODO: Update argument type and name
        fun onFragmentInteraction(title: String)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         */
        @JvmStatic
        fun newInstance() = SearchFragment()
    }
}
