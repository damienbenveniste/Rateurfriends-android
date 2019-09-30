package com.rateurfriends.rateurfriends.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rateurfriends.rateurfriends.R
import com.rateurfriends.rateurfriends.models.Contact
import androidx.core.graphics.drawable.DrawableCompat
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.button.MaterialButton
import kotlin.random.Random
import android.app.Activity
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals


class AllContactsAdapter(
        private val contactMap: LinkedHashMap<String, Contact>,
        private val mContext: Context,
        private val textView: TextView
) : RecyclerView.Adapter<AllContactsAdapter.ContactViewHolder>() {

    private val FIRST_INVITE = 123
    private val RESEND_INVITE = 234
    private var invitedContact: Contact? = null



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(mContext).inflate(
                R.layout.single_invite_contact_view,
                null
        )
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = ArrayList(contactMap.values)[position]
        holder.contactNameTextView.text = contact.phoneName.capitalize()
        holder.phoneNumberTextView.text = contact.phoneNumber

        val unwrappedDrawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_person_black_24dp)!!.mutate()
        val wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable)
        val color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        DrawableCompat.setTint(wrappedDrawable, color)

        holder.inviteButton.isEnabled = true

        holder.pictureImageView.setImageDrawable(wrappedDrawable)

        if (contact.knownUser) {
            holder.inviteButton.visibility = View.GONE
//            holder.inviteButton.setOnClickListener {
//                //TODO: add permission to access sms
//                //TODO: change to invite
//            }
        } else if (contact.invited) {
            holder.inviteButton.visibility = View.VISIBLE
            holder.inviteButton.alpha = 0.2f

            // todo: to change. Just to prevent to send text to anyone else
            if (contact.phoneName == "AUser") {
                holder.inviteButton.setOnClickListener {
                    holder.inviteButton.isEnabled = false
                    holder.inviteButton.alpha = 1f
                    onShareClicked(contact, position)
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

    private fun rewardUser(contact: Contact) {
        val user = Globals.getInstance().user!!
        val increment = 1

        UserDAO.sendInvite(user.userId, contact,
                onSuccess = {
                    Globals.getInstance().user!!.incrementSpareCategories(increment)
                    textView.text = mContext
                            .getString(R.string.invite_friends_spare_qualities)
                            .format(user.spareCategories)
                },
                onFailure = {

                    println("Could not capture the invite")

                }
        )
    }

//    private fun resendInvite(contact: Contact) {
//        val smsManager = SmsManager.getDefault() as SmsManager
//
//        val text = mContext.getString(R.string.invite_friends_second_invite_message)
//
//        smsManager.sendTextMessage(
//                contact.phoneNumber,
//                null,
//                text,
//                null,
//                null)
//
//    }

    fun onActivityResult(requestCode: Int) {

        if (requestCode == FIRST_INVITE && invitedContact != null)
            rewardUser(invitedContact!!)
    }

    private fun sendSMS(contact: Contact, text: String, requestCode: Int) {
        val uri = Uri.parse("smsto:" + contact.phoneNumber)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", text)
        intent.putExtra("exit_on_sent", true)
        if (intent.resolveActivity(mContext.packageManager) != null) {
            (mContext as Activity).startActivityForResult(intent, requestCode)
        }

    }

    private fun onShareClicked(contact: Contact, position: Int) {

        var text: String? = null
        var requestCode: Int? = null
        invitedContact = contact
        if (contact.invited) {
            text = mContext.getString(R.string.invite_friends_second_invite_message)
            requestCode = RESEND_INVITE
        } else {
            text =  mContext.getString(R.string.invite_friends_first_invite_message)
            requestCode = FIRST_INVITE
            contact.invited = true
            contactMap[contact.phoneNumber] = contact
            this.notifyItemChanged(position)
        }
        sendSMS(contact, text, requestCode)
    }

//    private fun onShareClicked(contact: Contact, position: Int) {
//
//        val smsManager = SmsManager.getDefault() as SmsManager
//        val sentPI = PendingIntent.getBroadcast(mContext, 0, Intent( "SMS_SENT"), 0)
//
//
//        mContext.registerReceiver(
//                object : BroadcastReceiver() {
//                    override fun onReceive(arg0: Context, arg1: Intent) {
//                        when (resultCode) {
//                            Activity.RESULT_OK -> {
//                                rewardUser(contact)
//                            }
//                            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
//                            }
//                            SmsManager.RESULT_ERROR_NO_SERVICE -> {
//                            }
//                            SmsManager.RESULT_ERROR_NULL_PDU -> {
//                            }
//                            SmsManager.RESULT_ERROR_RADIO_OFF -> {
//                            }
//                        }
//                    }
//                }, IntentFilter( "SMS_SENT"))
//
//        val text =  mContext.getString(R.string.invite_friends_first_invite_message)
//
//        contact.invited = true
//        contactMap[contact.phoneNumber] = contact
//        this.notifyItemChanged(position)
//
//        smsManager.sendTextMessage(
//                contact.phoneNumber,
//                null,
//                text,
//                sentPI,
//                null)
//    }


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