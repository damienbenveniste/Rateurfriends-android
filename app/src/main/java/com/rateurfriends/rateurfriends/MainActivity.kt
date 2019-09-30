package com.rateurfriends.rateurfriends

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.rateurfriends.rateurfriends.fragments.*
import com.rateurfriends.rateurfriends.login.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.rateurfriends.rateurfriends.database.dao.PictureDAO
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Contact
import java.util.*
import kotlin.collections.LinkedHashMap
import com.rateurfriends.rateurfriends.models.User
import java.text.ParseException
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(),
        ProfileFragment.OnFragmentInteractionListener,
        HallOfFameFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        MarketPlaceFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private val PERMISSION_ALL = 79
    private val PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_CONTACTS
    )

    private val mOnNavigationItemSelectedListener = BottomNavigationView
            .OnNavigationItemSelectedListener { item ->
        when (item.itemId) {

            R.id.navigation_hall_of_fame -> {
                val fragment = HallOfFameFragment.newInstance()
                addFragment(fragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_friends_feed -> {
                val fragment = FeedFragment.newInstance()
                addFragment(fragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                val fragment = ProfileFragment.newInstance()
                addFragment(fragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_market_place -> {
                val fragment = MarketPlaceFragment.newInstance()
                addFragment(fragment)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                val fragment = SearchFragment.newInstance()
                addFragment(fragment)

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun addFragment(fragment: Fragment) {

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_container, fragment, fragment.javaClass.simpleName)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        val navigationDrawerView: NavigationView = findViewById(R.id.navigation_drawer_view)
        val headerView = navigationDrawerView.getHeaderView(0)
        val nameTextView = headerView.findViewById(R.id.nav_header_textView) as TextView
        val profileImageView = headerView.findViewById(R.id.nav_header_imageView) as ImageView
        drawer = findViewById(R.id.drawer_layout)

        setSupportActionBar(toolbar)
        navigationDrawerView.setNavigationItemSelectedListener(this)
        navigationDrawerView.bringToFront()

        toggle = ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        )

        nameTextView.text = FirebaseAuth.getInstance().currentUser!!.displayName!!.capitalize()

        if (FirebaseAuth.getInstance().currentUser?.photoUrl != null) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            PictureDAO.populateImageView(userId!!, profileImageView, this)
        }

        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null
        navigation.selectedItemId = R.id.navigation_friends_feed

        checkPermissionAndFriends()
    }

    private fun checkPermissionAndFriends() {

        if(!hasPermissions(*PERMISSIONS)){
            requestPermissions(PERMISSIONS, PERMISSION_ALL)
        } else {
            checkFriends()
        }
    }

    private fun checkFriends() {
        val prefs = this.getSharedPreferences(
                this.getString(R.string.shared_preference_file),
                Context.MODE_PRIVATE
        )

        val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)

        try {
            val now = Date()
            val date = sdf.parse(prefs.getString(
                    "last_time_check_friends",
                    sdf.format(now)))

            val diff = now.time - date.time
            val diffDays = diff / (24 * 60 * 60 * 1000)

            if (diffDays > 7) {
                val runner = AsyncTaskRunner(contentResolver)
                runner.execute()

                prefs.edit()
                        .putString("last_time_check_friends", sdf.format(now))
                        .apply()
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun hasPermissions(vararg permissions: String): Boolean = permissions.all {
        checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {

        when (requestCode) {
            PERMISSION_ALL -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    checkFriends()

                }
                return
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.signout_item -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.invite_item -> {
                val intent = Intent(this@MainActivity,
                        InviteFriendsMenuActivity::class.java)
                startActivity(intent)
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onFragmentInteraction(title: String) {
        supportActionBar!!.title = title
    }

    class AsyncTaskRunner(
            private val contentResolver: ContentResolver
    ): AsyncTask<Void, Int, String>() {

        private val contactMap: LinkedHashMap<String, Contact> = linkedMapOf()
        private val CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        private val PROJECTION = arrayOf(
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        override fun doInBackground(vararg params: Void?): String? {

            val cursor = contentResolver
                    .query(
                            CONTENT_URI,
                            PROJECTION,
                            "HAS_PHONE_NUMBER <> 0",
                            null,
                            null
                    )

            if (cursor != null) {

                val displayNameIndex = cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                )
                val phoneIndex = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                while (cursor.moveToNext()) {

                    val displayName = cursor.getString(displayNameIndex)
                    var phoneNumber = cursor.getString(phoneIndex)
                    phoneNumber = PhoneNumberUtils.formatNumberToE164(
                            phoneNumber,
                            Locale.getDefault().country
                    )

                    if (phoneNumber != null &&
                            phoneNumber != Globals.getInstance().user!!.phoneNumber) {

                        val contact = Contact(displayName, phoneNumber)
                        contactMap[phoneNumber] = contact

                    }
                }
                cursor.close()
            }


            return null
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val userId = Globals.getInstance().user!!.userId
            contactMap.forEach {
                UserDAO.checkUserExistWithPhone(it.value.phoneNumber,
                        onSuccess = {
                            documentSnapshots ->
                            if (!documentSnapshots.isEmpty) {
                                val document = documentSnapshots.documents.firstOrNull()
                                if (document != null) {

                                    val user = document.toObject(User::class.java)!!

                                    if (contactMap.containsKey(user.phoneNumber)) {

                                        contactMap[user.phoneNumber]!!.userId = user.userId

                                        UserDAO.insertNewContact(
                                                contactMap[user.phoneNumber]!!,
                                                userId,
                                                onFailure = {
                                                    println("Contact could not be added")
                                                }
                                        )
                                    }

                                }
                            }
                        },
                        onFailure = {
                            println("Could not check if user exits")
                        }
                )
            }
        }
    }
}
