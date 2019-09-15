package com.rateurfriends.rateurfriends.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rateurfriends.rateurfriends.MainActivity
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.controllers.LoginController

class LoginActivity : AppCompatActivity() {

    private var loginController: LoginController? = null
    private var firstTimeIntent: Intent? = null
    private var nextTimeIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginController = LoginController(this)

        firstTimeIntent = Intent(
                this@LoginActivity,
                InfoCollectionActivity::class.java
        )

        nextTimeIntent = Intent(
                this@LoginActivity,
                MainActivity::class.java
        )

        firstTimeIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        nextTimeIntent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        loginController!!.handleUserLoginIntent(firstTimeIntent!!, nextTimeIntent!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        loginController!!.handleOnActivityResult(
                requestCode,
                resultCode,
                data,
                firstTimeIntent!!,
                nextTimeIntent!!
        )
    }


}
