package com.rateurfriends.rateurfriends.login

import android.content.Intent
import android.os.Bundle
import com.rateurfriends.rateurfriends.R
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.rateurfriends.rateurfriends.InviteFriendsActivity
import com.rateurfriends.rateurfriends.controllers.InfoCollectionController


class InfoCollectionActivity : AppCompatActivity() {

    private var infoCollectionController: InfoCollectionController? = null
    private var nameEditText: EditText? = null
    private var submitButton: MaterialButton? = null
    private var progressLayout: FrameLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_collection)

        infoCollectionController = InfoCollectionController(this)

        nameEditText = findViewById(R.id.et_name)
        submitButton = findViewById(R.id.bt_submit)
        progressLayout = findViewById(R.id.progress_layout)

        val intent = Intent(
                this@InfoCollectionActivity,
                InviteFriendsActivity::class.java)

        submitButton!!.setOnClickListener{
            infoCollectionController!!.submitInformation(nameEditText!!, intent, progressLayout!!)
        }
    }
}
