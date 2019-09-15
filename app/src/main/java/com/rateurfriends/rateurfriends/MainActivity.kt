package com.rateurfriends.rateurfriends

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
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
import android.widget.Toast
import com.rateurfriends.rateurfriends.controllers.PictureController
import com.rateurfriends.rateurfriends.database.dao.PictureDAO


class MainActivity : AppCompatActivity(),
        ProfileFragment.OnFragmentInteractionListener,
        HallOfFameFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener,
        MarketPlaceFragment.OnFragmentInteractionListener,
        FeedFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

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

        nameTextView.text = FirebaseAuth.getInstance().currentUser!!.displayName

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
}
