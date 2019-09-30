package com.rateurfriends.rateurfriends

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.controllers.InviteFriendsController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.rateurfriends.rateurfriends.adapters.AllContactsAdapter
import com.rateurfriends.rateurfriends.models.Contact
import java.util.LinkedHashMap


class InviteFriendsActivity : AppCompatActivity() {

    private var rvContacts: RecyclerView? = null
    private var inviteFriendsController: InviteFriendsController? = null
    private var spareCategoriesTextView: TextView? = null
    private var progressLayout: FrameLayout? = null

    private val contactMap: LinkedHashMap<String, Contact> = linkedMapOf()
    private var contactAdapter: AllContactsAdapter? = null
    private var emptyLayout: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_friends)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        rvContacts = findViewById(R.id.rvContacts)
        spareCategoriesTextView = findViewById(R.id.tv_spare_categories)
        progressLayout = findViewById(R.id.progress_layout)
        emptyLayout = findViewById(R.id.empty_layout)

        rvContacts!!.addItemDecoration(
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        contactAdapter = AllContactsAdapter(contactMap, this, spareCategoriesTextView!!)
        rvContacts!!.adapter = contactAdapter
        rvContacts!!.layoutManager = LinearLayoutManager(this)


        inviteFriendsController = InviteFriendsController(this, progressLayout!!, emptyLayout!!)
        inviteFriendsController!!.requestPermission(
                contactMap,
                contactAdapter!!)

        inviteFriendsController!!.setTextView(spareCategoriesTextView!!)

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions:
                                            Array<String>,
                                            grantResults: IntArray) {

        inviteFriendsController!!.handlePermissions(
                requestCode,
                grantResults,
                contactMap,
                contactAdapter!!
        )

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_invite_contact, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_invite_next) {

            val prefs = getSharedPreferences(
                    getString(R.string.shared_preference_file),
                    Context.MODE_PRIVATE
            )

            if (prefs.getBoolean(getString(R.string.first_invite), true)) {
                prefs.edit().putBoolean(getString(R.string.first_invite), false).apply()
            }

            val intent = Intent(
                    this@InviteFriendsActivity,
                    MainActivity::class.java
            )

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        contactAdapter!!.onActivityResult(requestCode)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
