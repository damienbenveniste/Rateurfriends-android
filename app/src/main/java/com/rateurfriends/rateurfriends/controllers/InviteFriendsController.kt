package com.rateurfriends.rateurfriends.controllers

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import com.rateurfriends.rateurfriends.MainActivity
import com.rateurfriends.rateurfriends.adapters.AllContactsAdapter
import com.rateurfriends.rateurfriends.database.dao.UserDAO
import com.rateurfriends.rateurfriends.helperClasses.Globals
import com.rateurfriends.rateurfriends.models.Contact
import com.rateurfriends.rateurfriends.models.User
import java.util.*
import kotlin.collections.ArrayList
import android.widget.TextView
import com.rateurfriends.rateurfriends.R
import java.text.SimpleDateFormat


class InviteFriendsController(val activity: Activity,
                              val progressLayout: FrameLayout,
                              val emptyLayout:FrameLayout) {

    private val PERMISSION_ALL = 79
    private val PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_CONTACTS
    )

    fun setTextView(textView: TextView) {

        val user = Globals.getInstance().user!!
        textView.text = activity
                .getString(R.string.invite_friends_spare_qualities)
                .format(user.spareCategories)
    }

    fun requestPermission(map: LinkedHashMap<String, Contact>,
                                  adapter: AllContactsAdapter) {


        if(!hasPermissions(activity, *PERMISSIONS)){
            ActivityCompat.requestPermissions(activity, PERMISSIONS, PERMISSION_ALL)
        } else {
            getContacts(map, adapter)
        }
    }

    private fun hasPermissions(context: Context,
                               vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun handlePermissions(requestCode: Int,
                          grantResults: IntArray,
                          map: LinkedHashMap<String, Contact>,
                          adapter: AllContactsAdapter) {

        when (requestCode) {
            PERMISSION_ALL -> {
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    getContacts(map, adapter)

                } else {
                    val intent = Intent(activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    activity.startActivity(intent)
                }
                return
            }
        }
    }

    private fun getContacts(map: LinkedHashMap<String, Contact>,
                            adapter: AllContactsAdapter) {

        progressLayout.visibility = View.VISIBLE

        UserDAO.getInvitedContactsForUser(Globals.getInstance().user!!.userId,
                onSuccess = {
                    val invitedPhoneSet = it.map{ contact -> contact.phoneNumber }.toSet()
                    val contentResolver = activity.contentResolver
                    val runner = AsyncTaskRunner(
                            contentResolver,
                            invitedPhoneSet,
                            map,
                            adapter,
                            progressLayout,
                            emptyLayout
                    )

                    runner.execute()

                    val prefs = activity.getSharedPreferences(
                            activity.getString(R.string.shared_preference_file),
                            Context.MODE_PRIVATE
                    )

                    val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US)
                    val now = Date()

                    prefs.edit()
                            .putString("last_time_check_friends", sdf.format(now))
                            .apply();

                },
                onFailure = {
                    println("Could not get invited contacts")
                }
        )

    }

    class AsyncTaskRunner(
            private val contentResolver: ContentResolver,
            private val phoneSet: Set<String>,
            private val contactMap: LinkedHashMap<String, Contact>,
            private val contactAdapter: AllContactsAdapter,
            private val progressLayout: FrameLayout,
            private val emptyLayout: FrameLayout
    ): AsyncTask<Void, Int, String>() {

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

//                var counter = 0
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
                        contact.invited = contact.phoneNumber in phoneSet
                        contactMap[phoneNumber] = contact

//                        if (counter == 10) {
//                            publishProgress(counter)
//                        }
//                        counter++
                    }
                }
                cursor.close()
            }


            return null
        }

//        override fun onProgressUpdate(vararg counter: Int?) {
//            super.onProgressUpdate(*counter)
//            progressLayout.visibility = View.GONE
//            contactAdapter.notifyDataSetChanged()
//
//        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            contactAdapter.notifyDataSetChanged()
            progressLayout.visibility = View.GONE

            if (contactMap.isEmpty()) {
                emptyLayout.visibility = View.VISIBLE

            } else {
                emptyLayout.visibility = View.GONE
            }

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

                                        contactMap[user.phoneNumber]!!.knownUser = true
                                        contactMap[user.phoneNumber]!!.userId = user.userId

                                        contactAdapter.notifyItemChanged(
                                                ArrayList(contactMap.keys).indexOf(user.phoneNumber)
                                        )

                                        UserDAO.insertNewContact(contactMap[user.phoneNumber]!!, userId,
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