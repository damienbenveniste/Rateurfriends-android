package com.rateurfriends.rateurfriends.controllers

import android.app.SearchManager
import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.adapters.ContactProfileAdapter
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.fragments.SearchFragment
import com.rateurfriends.rateurfriends.models.Contact

class SearchController(val fragment: SearchFragment) {


    private var contactList: ArrayList<Contact> = arrayListOf()
    private var contactAdapter: ContactProfileAdapter? = null

    fun getContacts() {

        fragment.progressLayout!!.visibility = View.VISIBLE
        contactAdapter = ContactProfileAdapter(contactList, fragment)
        fragment.rvContacts!!.adapter = contactAdapter
        fragment.rvContacts!!.layoutManager = LinearLayoutManager(fragment.activity)

        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        UserDAO.getContactsForUser(userId) {
            it.forEach { doc ->
                contactList.add(doc.toObject(Contact::class.java))
            }
            contactAdapter!!.notifyDataSetChanged()
            fragment.progressLayout!!.visibility = View.GONE

        }
    }

    fun setupSearch(searchItem: MenuItem?) {

        val searchManager = fragment.activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager

        if (searchItem != null) {
            fragment.searchView = searchItem.actionView as SearchView
        }
        if (fragment.searchView != null) {
            fragment.searchView!!.setSearchableInfo(
                            searchManager.getSearchableInfo(fragment.activity!!.componentName))

            val queryTextListener = object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(query: String): Boolean {
                    contactAdapter!!.filter.filter(query)

                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    contactAdapter!!.filter.filter(query)
                    return true
                }
            }
            fragment.searchView!!.setOnQueryTextListener(queryTextListener)
        }
    }




}