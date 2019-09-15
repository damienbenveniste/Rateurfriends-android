package com.rateurfriends.rateurfriends.fragments

import android.content.Context
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseAuth

import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.adapters.ProductsAdapter
import com.rateurfriends.rateurfriends.controllers.MarketPlaceController
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Product
import kotlinx.android.synthetic.main.fragment_market_place.*


class MarketPlaceFragment : Fragment(),
        PurchasesUpdatedListener {

    private var listener: OnFragmentInteractionListener? = null

    private var marketPlaceController: MarketPlaceController? = null

    var productRecyclerView: RecyclerView? = null
    var spareStarTextView: TextView? = null
    var spareCategoryTextView: TextView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_market_place, container, false)
        if (listener != null) {
            listener!!.onFragmentInteraction("Market Place");
        }
        marketPlaceController = MarketPlaceController(this)

        productRecyclerView = view.findViewById(R.id.products) as RecyclerView
        spareStarTextView = view.findViewById(R.id.tv_spare_stars) as TextView
        spareCategoryTextView = view.findViewById(R.id.tv_spare_categories) as TextView

        marketPlaceController!!.setupBillingClient()
        marketPlaceController!!.setupTextViews()

        return view
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?,
                                    purchases: MutableList<Purchase>?) {
        marketPlaceController!!.onPurchasesUpdated(billingResult, purchases)

    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(title:String) {
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
        fun newInstance() = MarketPlaceFragment()
    }
}
