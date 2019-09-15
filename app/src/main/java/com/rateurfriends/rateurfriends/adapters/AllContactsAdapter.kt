package com.rateurfriends.rateurfriends.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
//import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.models.Contact
import com.rateurfriends.rateurfriends.models.User
import androidx.core.graphics.drawable.DrawableCompat
import android.graphics.drawable.Drawable
import android.telephony.SmsManager
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.random.Random
import android.content.IntentFilter
import android.app.Activity
import android.content.BroadcastReceiver
import android.app.PendingIntent
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals


class AllContactsAdapter(
        private val contactMap: LinkedHashMap<String, Contact>,
        private val mContext: Context,
        private val textView: TextView
) : RecyclerView.Adapter<AllContactsAdapter.ContactViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(mContext).inflate(
                R.layout.single_invite_contact_view,
                null
        )
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = ArrayList(contactMap.values)[position]
        holder.contactNameTextView.text = contact.phoneName
        holder.phoneNumberTextView.text = contact.phoneNumber

        val unwrappedDrawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_person_black_24dp)!!.mutate()
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable!!)
        val color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        DrawableCompat.setTint(wrappedDrawable, color)

        holder.inviteButton.isEnabled = true

        holder.pictureImageView.setImageDrawable(wrappedDrawable)

        if (contact.knownUser) {
            holder.inviteButton.visibility = View.GONE
            holder.inviteButton.setOnClickListener {
                //TODO: add permission to access sms
                //TODO: change to invite
            }
        } else if (contact.invited) {
            holder.inviteButton.visibility = View.VISIBLE
            holder.inviteButton.alpha = 0.2f

            if (contact.phoneName == "AUser") {
                holder.inviteButton.setOnClickListener {
                    holder.inviteButton.isEnabled = false
                    holder.inviteButton.alpha = 1f
                    resendInvite(contact)
                }
            } else {
                holder.inviteButton.isClickable = false
            }

        } else {
            holder.inviteButton.visibility = View.VISIBLE
            holder.inviteButton.alpha = 1f
            // todo: to change. Just to prevent to send text to anyone else
            if (contact.phoneName == "AUser") {
                holder.inviteButton.setOnClickListener {
                    holder.inviteButton.isEnabled = false
                    onShareClicked(contact, position)
                }
            } else {
                holder.inviteButton.isClickable = false
            }

        }
    }

//    fun generateLongContentLink(): Uri {
//        val baseUrl = Uri.parse("https://rateurfriends.com/google-maps")
////        val domain = "https://rateurfriends.page.link"
//        val domain = "https://rateurfriends.com"
//
//        return FirebaseDynamicLinks.getInstance()
//                .createDynamicLink()
//                .setLink(baseUrl)
//                .setDomainUriPrefix(domain)
//                .setAndroidParameters(DynamicLink.AndroidParameters.Builder("com.rateurfriends.rateurfriends").build())
//                .buildDynamicLink().uri
//    }

//    fun generateShortContentLink(callback: (String) -> Unit) {
//
//        val uri = generateLongContentLink()
//        println(uri)
//        FirebaseDynamicLinks.getInstance()
//                .createDynamicLink()
//                .setLongLink(uri)
//                .buildShortDynamicLink()
//                .addOnSuccessListener {
//                    callback(it.shortLink.toString())
//                }.addOnFailureListener { println(it) }
//
//
//
//
//    }

    private fun rewardUser(contact: Contact, position: Int) {
        val user = Globals.getInstance().user!!
        val increment = 1

        UserDAO.sendInvite(user.userId, contact) {
            Globals.getInstance().user!!.incrementSpareCategories(increment)
            textView.text = "Spare Qualities: %d".format(user.spareCategories)
        }
    }

    private fun resendInvite(contact: Contact) {
        val smsManager = SmsManager.getDefault() as SmsManager

        val text = "Just a reminder, I started to use RateUrFriends! Join me there\n\n" +
                "Android: https://rateurfriends.com/android\n" +
                "IPhone: https://rateurfriends.com/iphone"

        smsManager.sendTextMessage(
                contact.phoneNumber,
                null,
                text,
                null,
                null)


    }

    private fun onShareClicked(contact: Contact, position: Int) {


//        rewardUser(contact)

        val smsManager = SmsManager.getDefault() as SmsManager

        val SENT = "SMS_SENT"
        val sentPI = PendingIntent.getBroadcast(mContext, 0, Intent(SENT), 0)

        mContext.registerReceiver(
                object : BroadcastReceiver() {
                    override fun onReceive(arg0: Context, arg1: Intent) {
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                rewardUser(contact, position)
                            }
                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                            }
                            SmsManager.RESULT_ERROR_NO_SERVICE -> {
                            }
                            SmsManager.RESULT_ERROR_NULL_PDU -> {
                            }
                            SmsManager.RESULT_ERROR_RADIO_OFF -> {
                            }
                        }
                    }
                }, IntentFilter(SENT))



        val text = "Hey, I started to use this fun new app RateUrFriends! Join me there\n\n" +
                "Android: https://rateurfriends.com/android\n" +
                "IPhone: https://rateurfriends.com/iphone"

        contact.invited = true
        contactMap[contact.phoneNumber] = contact
        this.notifyItemChanged(position)

        smsManager.sendTextMessage(
                contact.phoneNumber,
                null,
                text,
                sentPI,
                null)
    }


    override fun getItemCount(): Int {
        return contactMap.size
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var contactNameTextView: TextView
        internal var phoneNumberTextView: TextView
        internal var inviteButton: MaterialButton
        internal var pictureImageView: ImageView

        init {
            contactNameTextView = itemView.findViewById(R.id.invite_contact_name) as TextView
            phoneNumberTextView = itemView.findViewById(R.id.phone_number) as TextView
            inviteButton = itemView.findViewById(R.id.invite_button) as MaterialButton
            pictureImageView = itemView.findViewById(R.id.iv_picture) as ImageView

        }
    }
}