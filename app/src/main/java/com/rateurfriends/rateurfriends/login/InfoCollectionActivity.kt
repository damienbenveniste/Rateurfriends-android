package com.rateurfriends.rateurfriends.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.rateurfriends.rateurfriends.R
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
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

        nameEditText = findViewById(R.id.et_name) as EditText
        submitButton = findViewById(R.id.bt_submit) as MaterialButton
        progressLayout = findViewById(R.id.progress_layout)

        val intent = Intent(
                this@InfoCollectionActivity,
                InviteFriendsActivity::class.java)

        submitButton!!.setOnClickListener{
            infoCollectionController!!.submitInformation(nameEditText!!, intent, progressLayout!!)
        }
    }
}
